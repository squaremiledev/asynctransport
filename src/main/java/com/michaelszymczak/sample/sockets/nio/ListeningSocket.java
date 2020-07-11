package com.michaelszymczak.sample.sockets.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.michaelszymczak.sample.sockets.api.TransportEventsListener;
import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;

public class ListeningSocket implements AutoCloseable
{
    private final int port;
    private final long commandIdThatTriggeredListening;
    private final ConnectionIdSource connectionIdSource;
    private final TransportEventsListener transportEventsListener;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    ListeningSocket(
            final int port,
            final long commandIdThatTriggeredListening,
            final Selector selector,
            final ConnectionIdSource connectionIdSource,
            final TransportEventsListener transportEventsListener
    )
    {
        this.port = port;
        this.commandIdThatTriggeredListening = commandIdThatTriggeredListening;
        this.selector = selector;
        this.connectionIdSource = connectionIdSource;
        this.transportEventsListener = transportEventsListener;
    }

    int port()
    {
        return port;
    }

    SelectionKey listen() throws IOException
    {
        serverSocketChannel = ServerSocketChannel.open();
        // non-blocking mode, but in case something was missed, it should fail fast
        serverSocketChannel.socket().setSoTimeout(1);
        serverSocketChannel.configureBlocking(false);
        final SelectionKey selectionKey = serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        serverSocketChannel.bind(new InetSocketAddress(port));
        return selectionKey;
    }

    public ConnectedSocket acceptConnection() throws IOException
    {
        final SocketChannel acceptedSocketChannel = serverSocketChannel.accept();
        final Socket acceptedSocket = acceptedSocketChannel.socket();
        final long connectionId = connectionIdSource.newId();
        final ConnectedSocket connectedSocket = new ConnectedSocket(acceptedSocket.getLocalPort(), connectionId, acceptedSocket.getPort(), acceptedSocketChannel);
        transportEventsListener.onEvent(new ConnectionAccepted(
                connectedSocket.port(),
                commandIdThatTriggeredListening,
                connectedSocket.remotePort(),
                connectedSocket.connectionId()
        ));
        return connectedSocket;
    }

    @Override
    public void close()
    {
        Resources.close(serverSocketChannel);
    }

    @Override
    public String toString()
    {
        return "ListeningSocket{" +
               "port=" + port +
               ", serverSocketChannel=" + serverSocketChannel +
               ", commandIdThatTriggeredListening=" + commandIdThatTriggeredListening +
               ", selector=" + selector +
               '}';
    }
}
