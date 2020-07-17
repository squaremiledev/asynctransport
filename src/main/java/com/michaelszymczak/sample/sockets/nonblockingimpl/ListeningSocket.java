package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.api.events.TransportEventsListener;
import com.michaelszymczak.sample.sockets.support.Resources;

public class ListeningSocket implements AutoCloseable
{
    private final int port;
    private final long commandIdThatTriggeredListening;
    private final ConnectionIdSource connectionIdSource;
    private final TransportEventsListener transportEventsListener;
    private final ServerSocketChannel serverSocketChannel;

    ListeningSocket(
            final int port,
            final long commandIdThatTriggeredListening,
            final ConnectionIdSource connectionIdSource,
            final TransportEventsListener transportEventsListener
    ) throws IOException
    {
        this.port = port;
        this.commandIdThatTriggeredListening = commandIdThatTriggeredListening;
        this.connectionIdSource = connectionIdSource;
        this.transportEventsListener = transportEventsListener;
        this.serverSocketChannel = ServerSocketChannel.open();
        // non-blocking mode, but in case something was missed, it should fail fast
        serverSocketChannel.socket().setSoTimeout(1);
        serverSocketChannel.configureBlocking(false);
    }

    void listen() throws IOException
    {
        serverSocketChannel.bind(new InetSocketAddress(port));
    }

    public ServerSocketChannel serverSocketChannel()
    {
        return serverSocketChannel;
    }

    public NonBlockingConnection acceptConnection() throws IOException
    {
        final SocketChannel acceptedSocketChannel = serverSocketChannel.accept();
        acceptedSocketChannel.configureBlocking(false);
        final Socket acceptedSocket = acceptedSocketChannel.socket();
        final long connectionId = connectionIdSource.newId();
        final NonBlockingConnection connection = new NonBlockingConnection(
                acceptedSocket.getLocalPort(),
                connectionId,
                acceptedSocket.getPort(),
                acceptedSocketChannel,
                transportEventsListener::onEvent
        );
        transportEventsListener.onEvent(new ConnectionAccepted(
                connection.port(),
                commandIdThatTriggeredListening,
                connection.remotePort(),
                connection.connectionId(),
                connection.initialSenderBufferSize()
        ));
        return connection;
    }

    int port()
    {
        return port;
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
               '}';
    }
}
