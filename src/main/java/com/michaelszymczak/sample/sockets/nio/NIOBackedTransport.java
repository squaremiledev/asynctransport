package com.michaelszymczak.sample.sockets.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.michaelszymczak.sample.sockets.api.Transport;
import com.michaelszymczak.sample.sockets.api.TransportEventsListener;
import com.michaelszymczak.sample.sockets.api.commands.CloseConnection;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.commands.StopListening;
import com.michaelszymczak.sample.sockets.api.commands.TransportCommand;
import com.michaelszymczak.sample.sockets.api.events.CommandFailed;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.api.events.StoppedListening;
import com.michaelszymczak.sample.sockets.connection.ConnectionRepository;

public class NIOBackedTransport implements AutoCloseable, Transport, Workmen.NonBlockingWorkman
{
    private final TransportEventsListener transportEventsListener;
    private final ConnectionIdSource connectionIdSource = new ConnectionIdSource();
    private final List<ListeningSocket> listeningSockets;
    private final Selector listeningSelector;
    private final Selector connectionsSelector;
    private final ConnectionRepository connectionRepository = new ConnectionRepository();

    public NIOBackedTransport(final TransportEventsListener transportEventsListener) throws IOException
    {
        this.transportEventsListener = transportEventsListener;
        this.listeningSelector = Selector.open();
        this.connectionsSelector = Selector.open();
        this.listeningSockets = new ArrayList<>(10);
    }

    @Override
    public void handle(final TransportCommand command)
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

    private void handleConnectionCommand(final ConnectionCommand command)
    {
        if (!connectionRepository.contains(command.connectionId()))
        {
            transportEventsListener.onEvent(new CommandFailed(command, "Connection id not found"));
            return;
        }
        connectionRepository.findByConnectionId(command.connectionId()).handle(command);

        if (command instanceof CloseConnection)
        {
            closeConnection((CloseConnection)command);
        }
    }

    private void handleTransportCommand(final TransportCommand command)
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
            throw new IllegalArgumentException();
        }
    }


    @Override
    public void work()
    {
        listeningWork();
        connectionsWork();
    }

    private void connectionsWork()
    {
        final int availableCount;
        try
        {
            availableCount = connectionsSelector.selectNow();

            if (availableCount > 0)
            {
                final Iterator<SelectionKey> keyIterator = connectionsSelector.selectedKeys().iterator();
                while (keyIterator.hasNext())
                {
                    final SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (!key.isValid())
                    {
                        continue;
                    }
                    if (key.isReadable())
                    {
                        final Connection connection = (Connection)key.attachment();
                        connection.read();
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void listeningWork()
    {
        final int availableCount;
        try
        {
            availableCount = listeningSelector.selectNow();

            if (availableCount > 0)
            {
                final Iterator<SelectionKey> keyIterator = listeningSelector.selectedKeys().iterator();
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
                        final Connection connection = listeningSocket.acceptConnection();
                        final SocketChannel socketChannel = connection.channel();
                        final SelectionKey selectionKey = socketChannel.register(connectionsSelector, SelectionKey.OP_READ);
                        selectionKey.attach(connection);
                        connectionRepository.add(connection);
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
    public void close()
    {
        for (int k = 0; k < listeningSockets.size(); k++)
        {
            final ListeningSocket listeningSocket = listeningSockets.get(k);
            Resources.close(listeningSocket);
        }
        Resources.close(listeningSelector);
        Resources.close(connectionsSelector);
        Resources.close(connectionRepository);
    }

    private void handle(final Listen command)
    {
        try
        {
            final ListeningSocket listeningSocket = new ListeningSocket(command.port(), command.commandId(), connectionIdSource, transportEventsListener);
            try
            {
                listeningSocket.listen();
                final ServerSocketChannel serverSocketChannel = listeningSocket.serverSocketChannel();
                final SelectionKey selectionKey = serverSocketChannel.register(listeningSelector, SelectionKey.OP_ACCEPT);
                selectionKey.attach(listeningSocket);
            }
            catch (IOException e)
            {
                Resources.close(listeningSocket);
                transportEventsListener.onEvent(new CommandFailed(command, e.getMessage()));
                return;
            }
            listeningSockets.add(listeningSocket);
            transportEventsListener.onEvent(new StartedListening(command.port(), command.commandId()));
        }
        catch (IOException e)
        {
            // TODO: return failure
            throw new RuntimeException();
        }
    }

    private void closeConnection(final CloseConnection command)
    {
        // TODO: return failure if not found
        if (connectionRepository.contains(command.connectionId()))
        {
            try
            {
                connectionRepository.findByConnectionId(command.connectionId()).close();
            }
            catch (Exception e)
            {
                transportEventsListener.onEvent(new CommandFailed(command, e.getMessage()));
            }
        }
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
        transportEventsListener.onEvent(new CommandFailed(command, "No listening socket found on this port"));
    }
}
