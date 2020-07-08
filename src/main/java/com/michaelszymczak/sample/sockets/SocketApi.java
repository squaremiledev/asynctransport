package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.michaelszymczak.sample.sockets.events.CommandFailed;
import com.michaelszymczak.sample.sockets.events.Event;
import com.michaelszymczak.sample.sockets.events.StartedListening;
import com.michaelszymczak.sample.sockets.events.StoppedListening;

public class SocketApi implements AutoCloseable
{
    private final List<Acceptor> acceptors = new ArrayList<>(10);
    private final SessionIdSource sessionIdSource = new SessionIdSource();

    Event listen(final long currentRequestId, final int serverPort)
    {
        final long sessionId = sessionIdSource.newId();
        final Acceptor acceptor = new Acceptor(sessionId);
        try
        {
            acceptor.listen(serverPort);
        }
        catch (IOException e)
        {
            Resources.close(acceptor);
            return new CommandFailed(currentRequestId, sessionId, e.getMessage());


        }
        acceptors.add(acceptor);
        return new StartedListening(currentRequestId, sessionId);
    }

    Event stopListening(final long commandId, final long listeningSessionId)
    {
        for (int k = 0; k < acceptors.size(); k++)
        {
            if (acceptors.get(k).id() == listeningSessionId)
            {
                Resources.close(acceptors.get(k));
                return new StoppedListening(commandId, listeningSessionId);
            }
        }
        return new CommandFailed(commandId, listeningSessionId, "");
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
