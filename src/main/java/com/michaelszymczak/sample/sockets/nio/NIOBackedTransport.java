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
import com.michaelszymczak.sample.sockets.api.events.TransportEvent;
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
            handleCommand((ConnectionCommand)command);
        }
        else
        {
            handleCommand(command);
        }
    }

    private void handleCommand(final TransportCommand command)
    {
        if (command instanceof Listen)
        {
            final Listen cmd = (Listen)command;
            transportEventsListener.onEvent(listen(cmd.port(), cmd.commandId()));
        }
        else if (command instanceof StopListening)
        {
            final StopListening cmd = (StopListening)command;
            transportEventsListener.onEvent(stopListening(cmd.port(), cmd.commandId()));
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    private void handleCommand(final ConnectionCommand command)
    {
        if (!connectionRepository.contains(command.connectionId()))
        {
            // TODO: test correct command id
            transportEventsListener.onEvent(new CommandFailed(command.port(), -999, "Connection id not found"));
            return;
        }
        connectionRepository.findByConnectionId(command.connectionId()).handle(command);

        if (command instanceof CloseConnection)
        {
            final CloseConnection cmd = (CloseConnection)command;
            closeConnection(cmd.port(), cmd.connectionId());
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

    private TransportEvent listen(final int port, final long commandId)
    {
        try
        {
            final ListeningSocket listeningSocket = new ListeningSocket(port, commandId, connectionIdSource, transportEventsListener);
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
                return new CommandFailed(port, commandId, e.getMessage());
            }
            listeningSockets.add(listeningSocket);
            return new StartedListening(port, commandId);
        }
        catch (IOException e)
        {
            // TODO: return failure
            throw new RuntimeException();
        }
    }

    private void closeConnection(final int port, final long connectionId)
    {
        // TODO: return failure if not found
        if (connectionRepository.contains(connectionId))
        {
            try
            {
                connectionRepository.findByConnectionId(connectionId).close();
            }
            catch (Exception e)
            {
                transportEventsListener.onEvent(new CommandFailed(port, connectionId, e.getMessage()));
            }
        }
    }

    private TransportEvent stopListening(final int port, final long commandId)
    {
        for (int k = 0; k < listeningSockets.size(); k++)
        {
            if (listeningSockets.get(k).port() == port)
            {
                Resources.close(listeningSockets.get(k));
                return new StoppedListening(port, commandId);
            }
        }
        return new CommandFailed(port, commandId, "No listening socket found on this port");
    }
}
