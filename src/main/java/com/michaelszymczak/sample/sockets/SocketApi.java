package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.michaelszymczak.sample.sockets.events.CommandFailed;
import com.michaelszymczak.sample.sockets.events.StartedListening;
import com.michaelszymczak.sample.sockets.events.StoppedListening;
import com.michaelszymczak.sample.sockets.events.TransportEvent;

public class SocketApi implements AutoCloseable
{
    private final List<Acceptor> acceptors = new ArrayList<>(10);

    TransportEvent listen(final long commandId, final int serverPort)
    {
        final Acceptor acceptor = new Acceptor(serverPort);
        try
        {
            acceptor.listen(serverPort);
        }
        catch (IOException e)
        {
            Resources.close(acceptor);
            return new CommandFailed(commandId, serverPort, e.getMessage());


        }
        acceptors.add(acceptor);
        return new StartedListening(commandId, serverPort);
    }

    TransportEvent stopListening(final long commandId, final int port)
    {
        for (int k = 0; k < acceptors.size(); k++)
        {
            if (acceptors.get(k).id() == port)
            {
                Resources.close(acceptors.get(k));
                return new StoppedListening(commandId, port);
            }
        }
        return new CommandFailed(commandId, port, "");
    }

    public void doWork()
    {
//        final int availableCount;
//        try
//        {
//            availableCount = selector.selectNow();
//
//            if (availableCount > 0)
//            {
//                final Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
//                while (keyIterator.hasNext())
//                {
//                    final SelectionKey key = keyIterator.next();
//                    keyIterator.remove();
//                    if (!key.isValid())
//                    {
//                        continue;
//                    }
//                    if (key.isAcceptable())
//                    {
//                    }
//                }
//            }
//        }
//        catch (IOException e)
//        {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public void close()
    {
        for (int k = 0; k < acceptors.size(); k++)
        {
            final Acceptor acceptor = acceptors.get(k);
            Resources.close(acceptor);
        }
    }
}
