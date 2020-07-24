package com.michaelszymczak.sample.sockets.domain.connection;

import com.michaelszymczak.sample.sockets.domain.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.domain.api.events.EventListener;
import com.michaelszymczak.sample.sockets.domain.api.events.TransportCommandFailed;

public class ConnectionService implements AutoCloseable
{
    public final ConnectionRepository connectionRepository;
    private final EventListener eventListener;

    public ConnectionService(final EventListener eventListener, final ConnectionRepository connectionRepository)
    {
        this.connectionRepository = connectionRepository;
        this.eventListener = eventListener;
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

}
