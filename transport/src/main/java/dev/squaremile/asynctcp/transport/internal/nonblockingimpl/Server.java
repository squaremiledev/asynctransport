package dev.squaremile.asynctcp.transport.internal.nonblockingimpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.agrona.CloseHelper;


import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.api.values.DelineationType;
import dev.squaremile.asynctcp.transport.internal.domain.CommandFactory;
import dev.squaremile.asynctcp.transport.internal.domain.connection.Connection;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionConfiguration;

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

    public Connection createConnection(final SocketChannel acceptedSocketChannel, final DelineationType delineation) throws SocketException
    {
        final Socket acceptedSocket = acceptedSocketChannel.socket();

        SocketAddress remoteSocketAddress = acceptedSocket.getRemoteSocketAddress();
        ConnectionIdValue connectionId = new ConnectionIdValue(acceptedSocket.getLocalPort(), connectionIdSource.newId());
        final ConnectionConfiguration configuration = new ConnectionConfiguration(
                connectionId,
                hostname(remoteSocketAddress),
                acceptedSocket.getPort(),
                acceptedSocketChannel.socket().getSendBufferSize(),
                // TODO [perf]: decide how to select buffer size (prod and test performance)
                acceptedSocketChannel.socket().getSendBufferSize() * 2,
                acceptedSocketChannel.socket().getReceiveBufferSize()
        );
        return new ConnectionImpl(configuration, new SocketBackedChannel(acceptedSocketChannel), delineation, eventListener::onEvent);
    }

    SocketChannel acceptChannel() throws IOException
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
