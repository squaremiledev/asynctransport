package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.michaelszymczak.sample.sockets.domain.api.ConnectionId;
import com.michaelszymczak.sample.sockets.domain.api.Transport;
import com.michaelszymczak.sample.sockets.domain.api.commands.CommandFactory;
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
import com.michaelszymczak.sample.sockets.domain.connection.ConnectionService;
import com.michaelszymczak.sample.sockets.domain.connection.ConnectionState;

import org.agrona.CloseHelper;
import org.agrona.collections.Int2ObjectHashMap;

import static org.agrona.LangUtil.rethrowUnchecked;

public class NonBlockingTransport implements AutoCloseable, Transport
{
    private final ConnectionIdSource connectionIdSource = new ConnectionIdSource();
    private final Int2ObjectHashMap<ListeningSocket> listeningSocketsByPort = new Int2ObjectHashMap<>();
    private final Selector selector = Selector.open();
    private final ConnectionService connectionService;
    private final CommandFactory commandFactory = new CommandFactory();
    private final EventListener eventListener;
    private final Connections connections;

    public NonBlockingTransport(final EventListener eventListener) throws IOException
    {
        this.eventListener = eventListener;
        this.connections = new Connections(eventListener::onEvent);
        this.connectionService = new ConnectionService(eventListener, connections.connectionRepository);
    }

    private void handle(final ConnectionCommand command)
    {
        ConnectionState state = connectionService.handle(command);
        // means error (connection not found or invalid command)
        if (state == null)
        {
            return;
        }

        SelectionKey key = connections.getSelectionKey(command.connectionId());
        if (key == null)
        {
            return;
        }
        updateSelectionKeyInterest(state, key);
        if (state == ConnectionState.CLOSED)
        {
            connections.remove(command.connectionId());
        }
    }

    @Override
    public void work()
    {
        try
        {
            acceptingWork();
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
            rethrowUnchecked(e);
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

    private void tryHandle(final TransportCommand command) throws IOException
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

    private void acceptingWork() throws IOException
    {
        final int availableCount;

        availableCount = selector.selectNow();

        if (availableCount > 0)
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
                    final ListeningSocket listeningSocket = listeningSocketsByPort.get(port);
                    final SocketChannel acceptedSocketChannel = listeningSocket.acceptChannel();
                    final Connection connection = listeningSocket.createConnection(acceptedSocketChannel);
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
                else
                {
                    ConnectionCommand command = ((ConnectionConductor)key.attachment()).command(key);
                    ConnectionState state = connectionService.handle(command);
                    updateSelectionKeyInterest(state, key);
                    if (state == ConnectionState.CLOSED)
                    {
                        connections.remove(command.connectionId());
                    }
                }
            }
        }
    }

    @Override
    public void close()
    {
        CloseHelper.close(connections);
        CloseHelper.closeAll(listeningSocketsByPort.values());
        CloseHelper.close(selector);
        CloseHelper.close(connectionService);
    }

    private void handle(final Listen command) throws IOException
    {
        final ListeningSocket listeningSocket = new ListeningSocket(command.port(), command.commandId(), connectionIdSource, eventListener, commandFactory);
        try
        {
            listeningSocket.listen();
            final ServerSocketChannel serverSocketChannel = listeningSocket.serverSocketChannel();
            final SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            selectionKey.attach(new ListeningSocketConductor(listeningSocket.port()));
            listeningSocketsByPort.put(listeningSocket.port(), listeningSocket);
            eventListener.onEvent(new StartedListening(command.port(), command.commandId()));
        }
        catch (IOException e)
        {
            CloseHelper.close(listeningSocket);
            eventListener.onEvent(new TransportCommandFailed(command, e.getMessage()));
        }
    }

    private void handle(final StopListening command)
    {
        if (!listeningSocketsByPort.containsKey(command.port()))
        {
            eventListener.onEvent(new TransportCommandFailed(command, "No listening socket found on this port"));
            return;
        }

        CloseHelper.close(listeningSocketsByPort.remove(command.port()));
        eventListener.onEvent(new StoppedListening(command.port(), command.commandId()));
    }

}
