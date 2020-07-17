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
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.commands.StopListening;
import com.michaelszymczak.sample.sockets.api.commands.TransportCommand;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.api.events.StoppedListening;
import com.michaelszymczak.sample.sockets.api.events.TransportCommandFailed;
import com.michaelszymczak.sample.sockets.api.events.TransportEventsListener;
import com.michaelszymczak.sample.sockets.connection.ConnectionAggregate;
import com.michaelszymczak.sample.sockets.connection.ConnectionRepository;

public class NIOBackedTransport implements AutoCloseable, Transport
{
    private final TransportEventsListener transportEventsListener;
    private final ConnectionIdSource connectionIdSource = new ConnectionIdSource();
    private final List<ListeningSocket> listeningSockets;
    private final Selector acceptingSelector;
    private final Selector connectionsSelector;
    private final ConnectionRepository connectionRepository = new ConnectionRepository();

    public NIOBackedTransport(final TransportEventsListener transportEventsListener) throws IOException
    {
        this.transportEventsListener = transportEventsListener;
        this.acceptingSelector = Selector.open();
        this.connectionsSelector = Selector.open();
        this.listeningSockets = new ArrayList<>(10);
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

    private void handleConnectionCommand(final ConnectionCommand command)
    {
        if (!connectionRepository.contains(command.connectionId()))
        {
            transportEventsListener.onEvent(new TransportCommandFailed(command, "Connection id not found"));
            return;
        }

        // TODO: handle a graceful FIN and abrupt RST sending appropriate events
        final ConnectionAggregate targetConnection = connectionRepository.findByConnectionId(command.connectionId());
        targetConnection.handle(command);

        if (targetConnection.isClosed())
        {
            connectionRepository.removeById(command.connectionId());
        }
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
                if (!key.isValid())
                {
                    continue;
                }
                if (key.isReadable())
                {
                    final Connection connection = (Connection)key.attachment();
                    connection.read();
                }
                if (key.attachment() != null && (((Connection)key.attachment()).isClosed()))
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
                    final Connection connection = listeningSocket.acceptConnection();
                    final SocketChannel socketChannel = connection.channel();
                    final SelectionKey selectionKey = socketChannel.register(connectionsSelector, SelectionKey.OP_READ);
                    selectionKey.attach(connection);
                    connectionRepository.add(connection);
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
        Resources.close(connectionRepository);
    }

    private void handle(final Listen command) throws IOException
    {
        final ListeningSocket listeningSocket = new ListeningSocket(command.port(), command.commandId(), connectionIdSource, transportEventsListener);
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
