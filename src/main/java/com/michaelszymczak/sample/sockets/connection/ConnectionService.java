package com.michaelszymczak.sample.sockets.connection;

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

    public void newConnection(final Connection connection)
    {
        connectionRepository.add(connection);
    }

    public boolean handle(final ConnectionCommand command)
    {
        if (!connectionRepository.contains(command.connectionId()))
        {
            transportEventsListener.onEvent(new TransportCommandFailed(command, "Connection id not found"));
            return false;
        }

        final Connection connection = connectionRepository.findByConnectionId(command.connectionId());
        connection.handle(command);

        if (connection.isClosed())
        {
            connectionRepository.removeById(command.connectionId());
            return true;
        }
        return false;
    }

    @Override
    public void close()
    {
        connectionRepository.close();
    }
}
