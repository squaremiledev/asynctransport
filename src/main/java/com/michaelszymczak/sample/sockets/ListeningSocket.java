package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class ListeningSocket implements AutoCloseable
{
    private final int port;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    ListeningSocket(final int port)
    {
        this.port = port;
    }

    int port()
    {
        return port;
    }

    void listen(final int serverPort) throws IOException
    {
        serverSocketChannel = ServerSocketChannel.open();
        selector = Selector.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        serverSocketChannel.bind(new InetSocketAddress(serverPort));
    }

    @Override
    public void close()
    {
        Resources.close(serverSocketChannel);
        Resources.close(selector);
    }

    @Override
    public String toString()
    {
        return "ListeningSocket{" +
               "port=" + port +
               ", serverSocketChannel=" + serverSocketChannel +
               ", selector=" + selector +
               '}';
    }
}
