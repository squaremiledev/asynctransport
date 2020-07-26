package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.michaelszymczak.sample.sockets.domain.api.ConnectionId;
import com.michaelszymczak.sample.sockets.domain.api.ConnectionIdValue;
import com.michaelszymczak.sample.sockets.domain.api.Transport;
import com.michaelszymczak.sample.sockets.domain.api.TransportId;
import com.michaelszymczak.sample.sockets.domain.api.commands.CommandFactory;
import com.michaelszymczak.sample.sockets.domain.api.commands.Connect;
import com.michaelszymczak.sample.sockets.domain.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.domain.api.commands.Listen;
import com.michaelszymczak.sample.sockets.domain.api.commands.NoOpCommand;
import com.michaelszymczak.sample.sockets.domain.api.commands.ReadData;
import com.michaelszymczak.sample.sockets.domain.api.commands.SendData;
import com.michaelszymczak.sample.sockets.domain.api.commands.StopListening;
import com.michaelszymczak.sample.sockets.domain.api.commands.TransportCommand;
import com.michaelszymczak.sample.sockets.domain.api.events.EventListener;
import com.michaelszymczak.sample.sockets.domain.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.domain.api.events.StoppedListening;
import com.michaelszymczak.sample.sockets.domain.api.events.TransportCommandFailed;
import com.michaelszymczak.sample.sockets.domain.connection.Connection;
import com.michaelszymczak.sample.sockets.domain.connection.ConnectionConfiguration;
import com.michaelszymczak.sample.sockets.domain.connection.ConnectionState;

import org.agrona.CloseHelper;
import org.agrona.LangUtil;

public class NonBlockingTransport implements AutoCloseable, Transport
{
    private final ConnectionIdSource connectionIdSource = new ConnectionIdSource();
    private final Selector selector = Selector.open();
    private final CommandFactory commandFactory = new CommandFactory();
    private final EventListener eventListener;
    private final Connections connections;
    private final Servers servers;

    public NonBlockingTransport(final EventListener eventListener) throws IOException
    {
        this.servers = new Servers();
        this.connections = new Connections(eventListener::onEvent);
        this.eventListener = eventListener;
    }

    private void handle(final ConnectionCommand command)
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
        try
        {
            if (selector.selectNow() > 0)
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
                        int port = ((ListeningSocketConductor)key.attachment()).port();
                        final Server server = servers.serverListeningOn(port);
                        final SocketChannel acceptedSocketChannel = server.acceptChannel();
                        final Connection connection = server.createConnection(acceptedSocketChannel);
                        connections.add(connection, acceptedSocketChannel.register(
                                selector,
                                SelectionKey.OP_READ,
                                new ConnectionConductor(
                                        commandFactory.create(connection, ReadData.class),
                                        commandFactory.create(connection, SendData.class),
                                        commandFactory.create(connection, NoOpCommand.class)
                                )
                        ));
                    }
                    else if (key.isConnectable())
                    {
                        ConnectedNotification connectedNotification = (ConnectedNotification)key.attachment();
                        connectedNotification.socketChannel.finishConnect();
                        if (connectedNotification.socketChannel.isConnected())
                        {
                            Connection connection = connections.get(connectedNotification.connectionId);
                            connection.connected(connectedNotification.socketChannel.socket().getLocalPort(), connectedNotification.commandId);
                            // TODO: re-register as above ?
                            connectedNotification.socketChannel.register(selector, 0);
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
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(final TransportCommand command)
    {
        try
        {
            tryHandle(command);
        }
        catch (Exception e)
        {
            eventListener.onEvent(new TransportCommandFailed(command, e.getMessage()));
        }
    }

    @Override
    public <C extends TransportCommand> C command(final Class<C> commandType)
    {
        return commandFactory.create(commandType);
    }

    @Override
    public <C extends ConnectionCommand> C command(final ConnectionId connectionId, final Class<C> commandType)
    {
        Connection connection = connections.get(connectionId.connectionId());
        return connection != null ? connection.command(commandType) : null;
    }

    private void tryHandle(final TransportCommand command)
    {
        if (command instanceof ConnectionCommand)
        {
            handle((ConnectionCommand)command);
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

    @Override
    public void close()
    {
        CloseHelper.close(connections);
        CloseHelper.closeAll(servers);
        CloseHelper.close(selector);
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
            selectionKey.attach(new ListeningSocketConductor(server.port()));
            eventListener.onEvent(new StartedListening(command.port(), command.commandId()));
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
            long connectionId = connectionIdSource.newId();
            // TODO: retrieve a host from the command
            socketChannel.connect(new InetSocketAddress("localhost", command.port()));
            Socket socket = socketChannel.socket();
            // TODO: size buffers correctly
            final ConnectionConfiguration configuration = new ConnectionConfiguration(
                    new ConnectionIdValue(TransportId.NO_PORT, connectionId),
                    socket.getPort(),
                    10,
                    10,
                    10
            );
            final Connection connection = new ConnectionImpl(configuration, new SocketBackedChannel(socketChannel), eventListener::onEvent);
            // TODO: think of better mechanism than the notification object
            SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_CONNECT, new ConnectedNotification(connectionId, socketChannel, command.commandId()));
            connections.add(connection, selectionKey);
        }
        catch (IOException e)
        {
            LangUtil.rethrowUnchecked(e);
        }
    }

}
