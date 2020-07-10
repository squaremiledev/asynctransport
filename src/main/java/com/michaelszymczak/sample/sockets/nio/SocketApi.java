package com.michaelszymczak.sample.sockets.nio;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.michaelszymczak.sample.sockets.api.TransportEventsListener;
import com.michaelszymczak.sample.sockets.api.events.CommandFailed;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.api.events.StoppedListening;
import com.michaelszymczak.sample.sockets.api.events.TransportEvent;

public class SocketApi implements AutoCloseable
{
    private final List<ListeningSocket> listeningSockets;
    private final Selector listeningSelector;
    private final TransportEventsListener transportEventsListener;
    private ConnectionIdSource connectionIdSource;
    private Socket connectedSocket;

    public SocketApi(final TransportEventsListener transportEventsListener, final ConnectionIdSource connectionIdSource) throws IOException
    {
        this.transportEventsListener = transportEventsListener;
        this.listeningSelector = Selector.open();
        this.listeningSockets = new ArrayList<>(10);
        this.connectionIdSource = connectionIdSource;
    }

    TransportEvent listen(final int port, final long commandId)
    {

        final ListeningSocket listeningSocket = new ListeningSocket(port, commandId, listeningSelector, connectionIdSource, transportEventsListener);
        try
        {
            // from now on you can retrieve listening socket from the selection key key
            final SelectionKey selectionKey = listeningSocket.listen();
            selectionKey.attach(listeningSocket);
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
                        // TODO: there will be more than one
                        this.connectedSocket = ((ListeningSocket)key.attachment()).acceptConnection();
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public TransportEvent closeConnection(final int port, final long connectionId)
    {
        return null;
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
        Resources.close(connectedSocket);
    }
}
