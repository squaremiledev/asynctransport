package dev.squaremile.asynctcp.transport.internal.nonblockingimpl;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.agrona.CloseHelper;
import org.agrona.LangUtil;
import org.agrona.concurrent.EpochClock;
import org.agrona.nio.TransportPoller;


import dev.squaremile.asynctcp.transport.api.app.ConnectionCommand;
import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportCommandHandler;
import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.commands.StopListening;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.events.StoppedListening;
import dev.squaremile.asynctcp.transport.api.events.TransportCommandFailed;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.internal.domain.CommandFactory;
import dev.squaremile.asynctcp.transport.internal.domain.NoOpCommand;
import dev.squaremile.asynctcp.transport.internal.domain.ReadData;
import dev.squaremile.asynctcp.transport.internal.domain.connection.Connection;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionConfiguration;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionState;

// TODO [perf]: make sure all commands and events can be used without generating garbage
public class NonBlockingTransport extends TransportPoller implements AutoCloseable, Transport
{
    private final ConnectionIdSource connectionIdSource;
    private final Selector selector;
    private final CommandFactory commandFactory = new CommandFactory();
    private final EventListener eventListener;
    private final Connections connections;
    private final Servers servers;
    private final PendingConnections pendingConnections;
    private final EpochClock clock;
    private final TransportCommandHandler commandHandler;
    private final String role;

    public NonBlockingTransport(final EventListener eventListener, final TransportCommandHandler commandHandler, final EpochClock clock, final String role)
    {
        this.role = role;
        this.clock = clock;
        this.servers = new Servers();
        this.connections = new Connections(eventListener::onEvent);
        this.eventListener = eventListener;
        this.commandHandler = commandHandler;
        this.pendingConnections = new PendingConnections(clock, eventListener);
        this.connectionIdSource = new ConnectionIdSource();
        try
        {
            this.selector = Selector.open();
            SELECTED_KEYS_FIELD.set(selector, selectedKeySet);
            PUBLIC_SELECTED_KEYS_FIELD.set(selector, selectedKeySet);
        }
        catch (IOException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void handle(final ConnectionUserCommand command)
    {
        Connection connection = connections.get(command.connectionId());
        if (connection == null)
        {
            eventListener.onEvent(new TransportCommandFailed(command, "Connection id not found"));
            return;
        }
        connection.handle(command);
        updateSelectionKeyInterest(connection.state(), connections.getSelectionKey(command.connectionId()));
        if (connection.state() == ConnectionState.CLOSED)
        {
            connections.remove(command.connectionId());
        }
    }

    @Override
    public void work()
    {
        pendingConnections.work();
        try
        {
            if (selector.selectNow() > 0)
            {
                final SelectionKey[] keys = selectedKeySet.keys();
                for (int i = 0, length = selectedKeySet.size(); i < length; i++)
                {
                    final SelectionKey key = keys[i];
                    if (!key.isValid())
                    {
                        continue;
                    }
                    if (key.isAcceptable())
                    {
                        ListeningSocketContext listeningSocketContext = (ListeningSocketContext)key.attachment();
                        int port = listeningSocketContext.port();
                        final Server server = servers.serverListeningOn(port);
                        final SocketChannel acceptedSocketChannel = server.acceptChannel();
                        long connectionId = registerConnection(acceptedSocketChannel, server.createConnection(acceptedSocketChannel, listeningSocketContext.delineation()));
                        connections.get(connectionId).accepted(server.commandIdThatTriggeredListening());
                    }
                    else if (key.isConnectable())
                    {
                        ConnectedNotification connectedNotification = pendingConnections.pendingConnection(key);
                        SocketChannel socketChannel = connectedNotification.socketChannel;
                        socketChannel.socket().setTcpNoDelay(true);
                        try
                        {
                            socketChannel.finishConnect();
                        }
                        catch (ConnectException e)
                        {
                            eventListener.onEvent(new TransportCommandFailed(
                                    connectedNotification.port, connectedNotification.commandId, e.getMessage(), Connect.class
                            ));
                            pendingConnections.removePendingConnection(key);
                            key.cancel();
                        }
                        if (socketChannel.isConnected())
                        {
                            Socket socket = socketChannel.socket();
                            socket.setTcpNoDelay(true);
                            // TODO [perf]: size buffers correctly
                            ConnectionIdValue connectionId = new ConnectionIdValue(socket.getLocalPort(), connectionIdSource.newId());
                            final ConnectionConfiguration configuration = new ConnectionConfiguration(
                                    connectionId,
                                    connectedNotification.remoteHost,
                                    socket.getPort(),
                                    socket.getSendBufferSize(),
                                    // TODO [perf]: decide how to select buffer size (prod and test performance)
                                    socket.getSendBufferSize() * 2,
                                    socket.getReceiveBufferSize()
                            );
                            registerConnection(
                                    socketChannel,
                                    new ConnectionImpl(
                                            configuration,
                                            new SocketBackedChannel(socketChannel),
                                            connectedNotification.command.delineation(),
                                            eventListener::onEvent
                                    )
                            );
                            connections.get(connectionId.connectionId()).connected(connectedNotification.commandId);
                            pendingConnections.removePendingConnection(key);
                        }
                    }
                    else
                    {
                        ConnectionCommand command = ((ConnectionConductor)key.attachment()).command(key);
                        Connection connection = connections.get(command.connectionId());
                        connection.handle(command);
                        if (connection.state() == ConnectionState.CLOSED)
                        {
                            connections.remove(command.connectionId());
                        }
                    }
                }
                selectedKeySet.reset();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <C extends TransportUserCommand> C command(final Class<C> commandType)
    {
        return commandFactory.create(commandType);
    }

    @Override
    public <C extends ConnectionUserCommand> C command(final long connectionId, final Class<C> commandType)
    {
        Connection connection = connections.get(connectionId);
        if (connection == null)
        {
            throw new IllegalArgumentException("There is no connection " + connectionId);
        }
        return connection.command(commandType);
    }

    @Override
    public void handle(final TransportCommand command)
    {
        commandHandler.handle(command);
        tryHandle(command);
    }

    private void tryHandle(final TransportCommand command)
    {
        if (command instanceof ConnectionUserCommand)
        {
            handle((ConnectionUserCommand)command);
        }
        else if (command instanceof Listen)
        {
            handle((Listen)command);
        }
        else if (command instanceof StopListening)
        {
            handle((StopListening)command);
        }
        else if (command instanceof Connect)
        {
            handle((Connect)command);
        }
        else
        {
            throw new UnsupportedOperationException(command.getClass().getCanonicalName());
        }
    }

    @Override
    public void close()
    {
        CloseHelper.close(connections);
        CloseHelper.closeAll(servers);
        CloseHelper.close(selector);
    }

    private long registerConnection(final SocketChannel socketChannel, final Connection connection) throws ClosedChannelException
    {
        connections.add(connection, socketChannel.register(
                selector,
                SelectionKey.OP_READ,
                new ConnectionConductor(
                        new ReadData(connection),
                        new SendData(connection, 0),
                        new NoOpCommand(connection)
                )
        ));
        return connection.connectionId();
    }

    private void updateSelectionKeyInterest(final ConnectionState state, final SelectionKey key)
    {
        switch (state)
        {
            case NO_OUTSTANDING_DATA:
                if ((key.interestOps() & SelectionKey.OP_WRITE) != 0)
                {
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                }
                break;
            case DATA_TO_SEND_BUFFERED:
                if ((key.interestOps() & SelectionKey.OP_WRITE) == 0)
                {
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                }
                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                break;
            case CLOSED:
                key.cancel();
                key.attach(null);
                break;
        }
    }

    private void handle(final Listen command)
    {
        if (servers.isListeningOn(command.port()))
        {
            eventListener.onEvent(new TransportCommandFailed(command, "Address already in use"));
            return;
        }
        try
        {
            servers.start(command.port(), command.commandId(), connectionIdSource, eventListener, commandFactory);
            Server server = servers.serverListeningOn(command.port());
            final ServerSocketChannel serverSocketChannel = server.serverSocketChannel();
            final SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            selectionKey.attach(new ListeningSocketContext(server.port(), command.delineation()));
            eventListener.onEvent(new StartedListening(command.port(), command.commandId(), command.delineation()));
        }
        catch (IOException e)
        {
            servers.stop(command.port());
            eventListener.onEvent(new TransportCommandFailed(command, e.getMessage()));
        }
    }

    private void handle(final StopListening command)
    {
        if (!servers.isListeningOn(command.port()))
        {
            eventListener.onEvent(new TransportCommandFailed(command, "No listening socket found on this port"));
            return;
        }

        servers.stop(command.port());
        eventListener.onEvent(new StoppedListening(command.port(), command.commandId()));
    }

    private void handle(final Connect command)
    {
        try
        {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(command.remoteHost(), command.remotePort()));
            final SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_CONNECT);
            pendingConnections.add(new ConnectedNotification(socketChannel, command, clock.time() + command.timeoutMs(), selectionKey, command.delineation()));
        }
        catch (IOException e)
        {
            LangUtil.rethrowUnchecked(e);
        }
    }

    @Override
    public String toString()
    {
        return "NonBlockingTransport{" +
               "role='" + role + '\'' +
               '}';
    }
}
