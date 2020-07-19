package com.michaelszymczak.sample.sockets.connection;

import com.michaelszymczak.sample.sockets.api.ConnectionId;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.events.StatusEventListener;
import com.michaelszymczak.sample.sockets.api.events.TransportCommandFailed;
import com.michaelszymczak.sample.sockets.api.events.TransportEventsListener;

public class ConnectionService implements AutoCloseable
{
    private final ConnectionRepository connectionRepository;
    private final TransportEventsListener transportEventsListener;

    public ConnectionService(final TransportEventsListener transportEventsListener, final StatusEventListener statusEventListener)
    {
        this.connectionRepository = new ConnectionRepository(new ConnectionRepository.StatusRepositoryUpdates(statusEventListener));
        this.transportEventsListener = transportEventsListener;
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
            transportEventsListener.onEvent(new TransportCommandFailed(command, "Connection id not found"));
            return ConnectionState.UNDEFINED;
        }

        final Connection connection = connectionRepository.findByConnectionId(command.connectionId());
        connection.handle(command);
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
