package com.michaelszymczak.sample.sockets;

import java.io.IOException;

public class ReactiveConnections implements AutoCloseable
{
    private Acceptor acceptor;

    public long listen(final int serverPort)
    {
        try
        {
            acceptor = new Acceptor();
            acceptor.listen(serverPort);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return 0L;

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
        Resources.close(acceptor);
    }

    public long stopListening(final long responseId)
    {
        if (responseId != 0L)
        {
            return -1L;
        }

        Resources.close(acceptor);

        return 0L;
    }
}
