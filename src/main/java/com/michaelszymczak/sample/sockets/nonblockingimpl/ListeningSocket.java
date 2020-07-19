package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.michaelszymczak.sample.sockets.api.ConnectionIdValue;
import com.michaelszymczak.sample.sockets.api.commands.CommandFactory;
import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.api.events.TransportEventsListener;
import com.michaelszymczak.sample.sockets.connection.ConnectionConfiguration;
import com.michaelszymczak.sample.sockets.support.Resources;

public class ListeningSocket implements AutoCloseable
{
    private final int port;
    private final long commandIdThatTriggeredListening;
    private final ConnectionIdSource connectionIdSource;
    private final TransportEventsListener transportEventsListener;
    private final CommandFactory commandFactory;
    private final ServerSocketChannel serverSocketChannel;

    ListeningSocket(
            final int port,
            final long commandIdThatTriggeredListening,
            final ConnectionIdSource connectionIdSource,
            final TransportEventsListener transportEventsListener,
            final CommandFactory commandFactory
    ) throws IOException
    {
        this.port = port;
        this.commandIdThatTriggeredListening = commandIdThatTriggeredListening;
        this.connectionIdSource = connectionIdSource;
        this.transportEventsListener = transportEventsListener;
        this.commandFactory = commandFactory;
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

    public ChannelBackedConnection createConnection(final SocketChannel acceptedSocketChannel) throws SocketException
    {
        final Socket acceptedSocket = acceptedSocketChannel.socket();
        final ConnectionConfiguration configuration = new ConnectionConfiguration(
                new ConnectionIdValue(acceptedSocket.getLocalPort(), connectionIdSource.newId()),
                acceptedSocket.getPort(),
                acceptedSocketChannel.socket().getSendBufferSize(),
                // TODO: decide how to select buffer size (prod and test performance)
                acceptedSocketChannel.socket().getSendBufferSize() * 5
        );
        final ChannelBackedConnection connection = new ChannelBackedConnection(
                configuration,
                new SocketBackedChannel(acceptedSocketChannel),
                transportEventsListener::onEvent,
                commandFactory
        );
        transportEventsListener.onEvent(new ConnectionAccepted(connection, commandIdThatTriggeredListening, configuration.remotePort, configuration.maxMsgSize));
        return connection;
    }

    public SocketChannel acceptChannel() throws IOException
    {
        final SocketChannel acceptedSocketChannel = serverSocketChannel.accept();
        acceptedSocketChannel.configureBlocking(false);
        return acceptedSocketChannel;
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
               ", commandIdThatTriggeredListening=" + commandIdThatTriggeredListening +
               ", connectionIdSource=" + connectionIdSource +
               ", transportEventsListener=" + transportEventsListener +
               ", commandFactory=" + commandFactory +
               ", serverSocketChannel=" + serverSocketChannel +
               '}';
    }
}
