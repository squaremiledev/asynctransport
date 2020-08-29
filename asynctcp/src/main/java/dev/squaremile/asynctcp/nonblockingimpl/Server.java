package dev.squaremile.asynctcp.nonblockingimpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.agrona.CloseHelper;


import dev.squaremile.asynctcp.domain.api.ConnectionIdValue;
import dev.squaremile.asynctcp.domain.api.commands.CommandFactory;
import dev.squaremile.asynctcp.domain.api.events.EventListener;
import dev.squaremile.asynctcp.domain.connection.Connection;
import dev.squaremile.asynctcp.domain.connection.ConnectionConfiguration;

public class Server implements AutoCloseable
{
    private final int port;
    private final long commandIdThatTriggeredListening;
    private final ConnectionIdSource connectionIdSource;
    private final EventListener eventListener;
    private final CommandFactory commandFactory;
    private final ServerSocketChannel serverSocketChannel;

    Server(
            final int port,
            final long commandIdThatTriggeredListening,
            final ConnectionIdSource connectionIdSource,
            final EventListener eventListener,
            final CommandFactory commandFactory
    ) throws IOException
    {
        this.port = port;
        this.commandIdThatTriggeredListening = commandIdThatTriggeredListening;
        this.connectionIdSource = connectionIdSource;
        this.eventListener = eventListener;
        this.commandFactory = commandFactory;
        this.serverSocketChannel = ServerSocketChannel.open();
        // non-blocking mode, but in case something was missed, it should fail fast
        serverSocketChannel.socket().setSoTimeout(1);
        serverSocketChannel.configureBlocking(false);
    }

    public long commandIdThatTriggeredListening()
    {
        return commandIdThatTriggeredListening;
    }

    void listen() throws IOException
    {
        serverSocketChannel.bind(new InetSocketAddress(port));
    }

    public ServerSocketChannel serverSocketChannel()
    {
        return serverSocketChannel;
    }

    public Connection createConnection(final SocketChannel acceptedSocketChannel) throws SocketException
    {
        final Socket acceptedSocket = acceptedSocketChannel.socket();
        final ConnectionConfiguration configuration = new ConnectionConfiguration(
                new ConnectionIdValue(acceptedSocket.getLocalPort(), connectionIdSource.newId()),
                acceptedSocket.getPort(),
                acceptedSocketChannel.socket().getSendBufferSize(),
                // TODO: decide how to select buffer size (prod and test performance)
                acceptedSocketChannel.socket().getSendBufferSize() * 2,
                acceptedSocketChannel.socket().getReceiveBufferSize()
        );
        return new ConnectionImpl(
                configuration,
                new SocketBackedChannel(acceptedSocketChannel),
                eventListener::onEvent
        );
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
        CloseHelper.close(serverSocketChannel);
    }

    @Override
    public String toString()
    {
        return "Server{" +
               "port=" + port +
               ", commandIdThatTriggeredListening=" + commandIdThatTriggeredListening +
               ", connectionIdSource=" + connectionIdSource +
               ", eventListener=" + eventListener +
               ", commandFactory=" + commandFactory +
               ", serverSocketChannel=" + serverSocketChannel +
               '}';
    }
}
