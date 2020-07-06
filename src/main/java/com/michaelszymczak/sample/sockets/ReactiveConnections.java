package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReactiveConnections implements AutoCloseable
{
    private final Map<Long, Acceptor> acceptors = new HashMap<>(10);
    private long requestIdGenerator = 0;

    public long listen(final int serverPort)
    {
        final long currentRequestId = requestIdGenerator++;
        try
        {
            final Acceptor acceptor = new Acceptor(currentRequestId);
            acceptor.listen(serverPort);
            acceptors.put(acceptor.id(), acceptor);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return currentRequestId;

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
        acceptors.values().forEach(Resources::close);
    }

    public long stopListening(final long listenRequestId)
    {
        if (!acceptors.containsKey(listenRequestId))
        {
            return -1L;
        }

        Resources.close(acceptors.get(listenRequestId));

        return 0L;
    }
}
