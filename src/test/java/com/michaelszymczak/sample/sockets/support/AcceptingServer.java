package com.michaelszymczak.sample.sockets.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;


import static java.nio.charset.StandardCharsets.US_ASCII;

public class AcceptingServer
{
    private final int serverPort;
    private final ServerReadiness onReady;

    public int port()
    {
        return serverPort;
    }

    public AcceptingServer(final int serverPort)
    {
        this.serverPort = FreePort.freePort(serverPort);
        this.onReady = new ServerReadiness();
    }

    public void waitUntilReady()
    {
        onReady.waitUntilReady();
    }

    public void startServer()
    {
        try (
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                Selector selector = Selector.open()
        )
        {
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverSocketChannel.bind(new InetSocketAddress(serverPort));
            onReady.onReady();
            while (!Thread.currentThread().isInterrupted())
            {
                final int availableCount = selector.selectNow();
                if (availableCount > 0)
                {
                    final Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
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
                            System.out.println("ACCEPTED");
                            final ServerSocketChannel serverChannel = ((ServerSocketChannel)key.channel());
                            final SocketChannel channel = serverChannel.accept();
                            if (channel != null)
                            {
                                channel.configureBlocking(false);
                                channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_CONNECT);
                                final ByteBuffer byteBuffer = ByteBuffer.wrap("hello!\n".getBytes(US_ASCII));
                                channel.write(byteBuffer);
                            }
                        }
                        else if (key.isConnectable())
                        {
                            System.out.println("CONNECT");
                        }
                        else if (key.isReadable())
                        {
                            System.out.println("READ");
                            SocketChannel channel = (SocketChannel)key.channel();
                            int read = channel.read(ByteBuffer.allocate(10));
                            System.out.println("read = " + read);
                        }
                        else if (key.isWritable())
                        {
                            System.out.println("WRITE");
                        }
                        else
                        {
                            throw new IllegalStateException();
                        }
                    }


                }
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(200));
            }
            System.out.println("Server shutting down...");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            System.out.println("Server shut down");
            onReady.onReady();
        }
    }
}
