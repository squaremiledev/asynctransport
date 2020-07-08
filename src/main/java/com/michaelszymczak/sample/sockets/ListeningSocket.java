package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import com.michaelszymczak.sample.sockets.events.ConnectionEstablished;
import com.michaelszymczak.sample.sockets.events.TransportEvent;

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

    SelectionKey listen(final Selector selector, final int serverPort) throws IOException
    {
        serverSocketChannel = ServerSocketChannel.open();
        // non-blocking mode, but in case something was missed, it should fail fast
        serverSocketChannel.socket().setSoTimeout(1);
        this.selector = selector;
        serverSocketChannel.configureBlocking(false);
        final SelectionKey selectionKey = serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        serverSocketChannel.bind(new InetSocketAddress(serverPort));
        return selectionKey;
    }

    public TransportEvent acceptConnection() throws IOException
    {
        serverSocketChannel.accept();
        return new ConnectionEstablished();
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
