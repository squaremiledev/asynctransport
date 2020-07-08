package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.michaelszymczak.sample.sockets.events.CommandFailed;
import com.michaelszymczak.sample.sockets.events.StartedListening;
import com.michaelszymczak.sample.sockets.events.StoppedListening;
import com.michaelszymczak.sample.sockets.events.TransportEvent;
import com.michaelszymczak.sample.sockets.events.TransportEventsListener;

public class SocketApi implements AutoCloseable
{
    private final List<ListeningSocket> listeningSockets;
    private final Selector listeningSelector;
    private final TransportEventsListener transportEventsListener;

    public SocketApi(final TransportEventsListener transportEventsListener) throws IOException
    {
        this.transportEventsListener = transportEventsListener;
        this.listeningSelector = Selector.open();
        this.listeningSockets = new ArrayList<>(10);
    }

    TransportEvent listen(final int port, final long commandId)
    {
        final ListeningSocket listeningSocket = new ListeningSocket(port);
        try
        {
            // from now on you can retrieve listening socket from the selection key key
            listeningSocket.listen(listeningSelector, port).attach(listeningSocket);
        }
        catch (IOException e)
        {
            Resources.close(listeningSocket);
            return new CommandFailed(port, commandId, e.getMessage());


        }
        listeningSockets.add(listeningSocket);
        return new StartedListening(port, commandId);
    }

    TransportEvent stopListening(final int port, final long commandId)
    {
        for (int k = 0; k < listeningSockets.size(); k++)
        {
            if (listeningSockets.get(k).port() == port)
            {
                Resources.close(listeningSockets.get(k));
                return new StoppedListening(port, commandId);
            }
        }
        return new CommandFailed(port, commandId, "");
    }

    public void doWork()
    {
        final int availableCount;
        try
        {
            availableCount = listeningSelector.selectNow();

            if (availableCount > 0)
            {
                final Iterator<SelectionKey> keyIterator = listeningSelector.selectedKeys().iterator();
                while (keyIterator.hasNext())
                {
                    final SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (!key.isValid())
                    {
                        continue;
                    }
                    if (key.isAcceptable())
                    {
                        transportEventsListener.onEvent(((ListeningSocket)key.attachment()).acceptConnection());
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close()
    {
        for (int k = 0; k < listeningSockets.size(); k++)
        {
            final ListeningSocket listeningSocket = listeningSockets.get(k);
            Resources.close(listeningSocket);
        }
        Resources.close(listeningSelector);
    }
}
