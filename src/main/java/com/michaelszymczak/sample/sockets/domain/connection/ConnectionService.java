package com.michaelszymczak.sample.sockets.domain.connection;

import com.michaelszymczak.sample.sockets.domain.api.ConnectionId;
import com.michaelszymczak.sample.sockets.domain.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.domain.api.events.EventListener;
import com.michaelszymczak.sample.sockets.domain.api.events.TransportCommandFailed;

public class ConnectionService implements AutoCloseable
{
    private final ConnectionRepository connectionRepository;
    private final EventListener eventListener;

    public ConnectionService(final EventListener eventListener)
    {
        this.connectionRepository = new ConnectionRepository(new ConnectionRepository.StatusRepositoryUpdates(eventListener::onEvent));
        this.eventListener = eventListener;
    }

    public <C extends Connection> C newConnection(final C connection)
    {
        connectionRepository.add(connection);
        return connection;
    }

    public ConnectionState handle(final ConnectionCommand command)
    {
        if (!connectionRepository.contains(command.connectionId()))
        {
            eventListener.onEvent(new TransportCommandFailed(command, "Connection id not found"));
            return null;
        }

        final Connection connection = connectionRepository.findByConnectionId(command.connectionId());
        boolean success = connection.handle(command);
        if (!success)
        {
            return null;
        }

        ConnectionState state = connection.state();
        if (state == ConnectionState.CLOSED)
        {
            connectionRepository.removeById(command.connectionId());
        }

        return state;
    }

    @Override
    public void close()
    {
        connectionRepository.close();
    }

    public <C extends ConnectionCommand> C command(final ConnectionId connectionId, final Class<C> commandType)
    {
        return connectionRepository.contains(connectionId.connectionId()) ? connectionRepository.findByConnectionId(connectionId.connectionId()).command(commandType) : null;
    }
}
