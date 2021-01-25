package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.agrona.CloseHelper;


import dev.squaremile.asynctcp.api.transport.app.EventListener;
import dev.squaremile.asynctcp.api.transport.values.ConnectionIdValue;
import dev.squaremile.asynctcp.api.transport.values.Delineation;
import dev.squaremile.asynctcp.internal.transport.domain.CommandFactory;
import dev.squaremile.asynctcp.internal.transport.domain.connection.Connection;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionConfiguration;

public class Server implements AutoCloseable
{
    private final String role;
    private final int port;
    private final long commandIdThatTriggeredListening;
    private final ConnectionIdSource connectionIdSource;
    private final EventListener eventListener;
    private final CommandFactory commandFactory;
    private final ServerSocketChannel serverSocketChannel;
    private final RelativeClock relativeClock;

    Server(
            final String role,
            final int port,
            final long commandIdThatTriggeredListening,
            final RelativeClock relativeClock,
            final ConnectionIdSource connectionIdSource,
            final EventListener eventListener,
            final CommandFactory commandFactory
    ) throws IOException
    {
        this.role = role;
        this.port = port;
        this.commandIdThatTriggeredListening = commandIdThatTriggeredListening;
        this.connectionIdSource = connectionIdSource;
        this.eventListener = eventListener;
        this.commandFactory = commandFactory;
        this.serverSocketChannel = ServerSocketChannel.open();
        // non-blocking mode, but in case something was missed, it should fail fast
        serverSocketChannel.socket().setSoTimeout(1);
        serverSocketChannel.configureBlocking(false);
        this.relativeClock = relativeClock;
    }

    private static String hostname(final SocketAddress remoteSocketAddress)
    {
        if (remoteSocketAddress instanceof InetSocketAddress)
        {
            InetSocketAddress remoteInetSocketAddress = (InetSocketAddress)remoteSocketAddress;
            return remoteInetSocketAddress.getHostName();
        }
        return remoteSocketAddress.toString();
    }

    public long commandIdThatTriggeredListening()
    {
        return commandIdThatTriggeredListening;
    }

    void listen() throws IOException
    {
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        serverSocketChannel.bind(inetSocketAddress);
    }

    public ServerSocketChannel serverSocketChannel()
    {
        return serverSocketChannel;
    }

    public Connection createConnection(final SocketChannel acceptedSocketChannel, final Delineation delineation) throws SocketException
    {
        final Socket acceptedSocket = acceptedSocketChannel.socket();

        SocketAddress remoteSocketAddress = acceptedSocket.getRemoteSocketAddress();
        ConnectionIdValue connectionId = new ConnectionIdValue(acceptedSocket.getLocalPort(), connectionIdSource.newId());
        final ConnectionConfiguration configuration = new ConnectionConfiguration(
                connectionId,
                hostname(remoteSocketAddress),
                acceptedSocket.getPort(),
                acceptedSocketChannel.socket().getSendBufferSize(),
                acceptedSocketChannel.socket().getSendBufferSize() * 16,
                acceptedSocketChannel.socket().getReceiveBufferSize()
        );
        return new ConnectionImpl(role, configuration, relativeClock, new SocketBackedChannel(acceptedSocketChannel), delineation, eventListener::onEvent);
    }

    SocketChannel acceptChannel() throws IOException
    {
        final SocketChannel acceptedSocketChannel = serverSocketChannel.accept();
        acceptedSocketChannel.socket().setTcpNoDelay(true);
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
