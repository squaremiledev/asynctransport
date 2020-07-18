package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.michaelszymczak.sample.sockets.api.ConnectionId;
import com.michaelszymczak.sample.sockets.api.Transport;
import com.michaelszymczak.sample.sockets.api.commands.CommandFactory;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.commands.NoOpCommand;
import com.michaelszymczak.sample.sockets.api.commands.ReadData;
import com.michaelszymczak.sample.sockets.api.commands.StopListening;
import com.michaelszymczak.sample.sockets.api.commands.TransportCommand;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.api.events.StatusEventListener;
import com.michaelszymczak.sample.sockets.api.events.StoppedListening;
import com.michaelszymczak.sample.sockets.api.events.TransportCommandFailed;
import com.michaelszymczak.sample.sockets.api.events.TransportEventsListener;
import com.michaelszymczak.sample.sockets.connection.ConnectionService;
import com.michaelszymczak.sample.sockets.support.Resources;

public class NonBlockingTransport implements AutoCloseable, Transport
{
    private final TransportEventsListener transportEventsListener;
    private final ConnectionIdSource connectionIdSource = new ConnectionIdSource();
    private final List<ListeningSocket> listeningSockets;
    private final Selector acceptingSelector;
    private final Selector connectionsSelector;
    private final ConnectionService connectionService;
    private final CommandFactory commandFactory;

    public NonBlockingTransport(final TransportEventsListener transportEventsListener, final StatusEventListener statusEventListener) throws IOException
    {
        this.transportEventsListener = transportEventsListener;
        this.acceptingSelector = Selector.open();
        this.connectionsSelector = Selector.open();
        this.listeningSockets = new ArrayList<>(10);
        this.connectionService = new ConnectionService(transportEventsListener, statusEventListener);
        this.commandFactory = new CommandFactory();
    }

    private void handleConnectionCommand(final ConnectionCommand command)
    {
        connectionService.handle(command);
    }

    private void handleTransportCommand(final TransportCommand command) throws Exception
    {
        if (command instanceof Listen)
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

    @Override
    public void work()
    {
        try
        {
            acceptingWork();
            connectionsWork();
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
            if (command instanceof ConnectionCommand)
            {
                handleConnectionCommand((ConnectionCommand)command);
            }
            else
            {
                handleTransportCommand(command);
            }
        }
        catch (Exception e)
        {
            transportEventsListener.onEvent(new TransportCommandFailed(command, e.getMessage()));
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
        return connectionService.command(connectionId, commandType);
    }

    private void connectionsWork() throws IOException
    {
        final int availableCount;

        availableCount = connectionsSelector.selectNow();

        if (availableCount > 0)
        {
            final Iterator<SelectionKey> keyIterator = connectionsSelector.selectedKeys().iterator();
            while (keyIterator.hasNext())
            {
                final SelectionKey key = keyIterator.next();
                keyIterator.remove();
                final boolean isClosed = connectionService.handle(((ConnectionConductor)key.attachment()).command(key));
                if (isClosed)
                {
                    key.cancel();
                    key.attach(null);
                }
            }
        }
    }

    private void acceptingWork() throws IOException
    {
        final int availableCount;

        availableCount = acceptingSelector.selectNow();

        if (availableCount > 0)
        {
            final Iterator<SelectionKey> keyIterator = acceptingSelector.selectedKeys().iterator();
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
                    final ListeningSocket listeningSocket = (ListeningSocket)key.attachment();
                    final SocketChannel acceptedSocketChannel = listeningSocket.acceptChannel();
                    final ChannelBackedConnection connection = connectionService.newConnection(listeningSocket.createConnection(acceptedSocketChannel));
                    acceptedSocketChannel.register(
                            connectionsSelector,
                            SelectionKey.OP_READ,
                            new ConnectionConductor(commandFactory.create(connection, ReadData.class), commandFactory.create(connection, NoOpCommand.class))
                    );
                }
            }
        }
    }

    @Override
    public void close()
    {
        for (int k = 0; k < listeningSockets.size(); k++)
        {
            final ListeningSocket listeningSocket = listeningSockets.get(k);
            Resources.close(listeningSocket);
        }
        Resources.close(acceptingSelector);
        Resources.close(connectionsSelector);
        Resources.close(connectionService);
    }

    private void handle(final Listen command) throws IOException
    {
        final ListeningSocket listeningSocket = new ListeningSocket(command.port(), command.commandId(), connectionIdSource, transportEventsListener, commandFactory);
        try
        {
            listeningSocket.listen();
            final ServerSocketChannel serverSocketChannel = listeningSocket.serverSocketChannel();
            final SelectionKey selectionKey = serverSocketChannel.register(acceptingSelector, SelectionKey.OP_ACCEPT);
            selectionKey.attach(listeningSocket);
        }
        catch (IOException e)
        {
            Resources.close(listeningSocket);
            transportEventsListener.onEvent(new TransportCommandFailed(command, e.getMessage()));
            return;
        }
        listeningSockets.add(listeningSocket);
        transportEventsListener.onEvent(new StartedListening(command.port(), command.commandId()));
    }

    private void handle(final StopListening command)
    {
        for (int k = 0; k < listeningSockets.size(); k++)
        {
            if (listeningSockets.get(k).port() == command.port())
            {
                Resources.close(listeningSockets.get(k));
                transportEventsListener.onEvent(new StoppedListening(command.port(), command.commandId()));
                return;
            }
        }
        transportEventsListener.onEvent(new TransportCommandFailed(command, "No listening socket found on this port"));
    }

}
