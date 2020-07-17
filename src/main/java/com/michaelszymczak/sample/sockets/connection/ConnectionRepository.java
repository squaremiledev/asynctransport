package com.michaelszymczak.sample.sockets.connection;

import java.util.HashMap;
import java.util.Map;

import com.michaelszymczak.sample.sockets.api.events.NumberOfConnectionsChanged;
import com.michaelszymczak.sample.sockets.api.events.StatusEventListener;
import com.michaelszymczak.sample.sockets.support.Resources;

public class ConnectionRepository implements AutoCloseable
{
    // TODO: autoboxing
    private final Map<Long, Connection> connections = new HashMap<>();
    private final RepositoryUpdates repositoryUpdates;

    public ConnectionRepository(final RepositoryUpdates repositoryUpdates)
    {
        this.repositoryUpdates = repositoryUpdates;
    }

    public void add(final Connection connection)
    {
        connections.put(connection.connectionId(), connection);
        notifyOfNumberOfConnections();
    }

    public int size()
    {
        return connections.size();
    }

    public Connection findByConnectionId(final long connectionId)
    {
        return connections.get(connectionId);
    }

    public boolean contains(final long connectionId)
    {
        return connections.containsKey(connectionId);
    }

    @Override
    public void close()
    {
        connections.values().forEach(Resources::close);
        connections.clear();
        notifyOfNumberOfConnections();
    }

    public void removeById(final long connectionId)
    {
        if (!connections.containsKey(connectionId))
        {
            return;
        }
        final Connection connection = connections.get(connectionId);
        if (!connection.isClosed())
        {
            throw new IllegalStateException("Connection must be closed before it's removed");
        }
        connections.remove(connectionId);
        notifyOfNumberOfConnections();
    }

    private void notifyOfNumberOfConnections()
    {
        repositoryUpdates.onNumberOfConnectionsChanged(connections.size());
    }

    public interface RepositoryUpdates
    {
        void onNumberOfConnectionsChanged(int newNumberOfConnections);
    }

    public static class StatusRepositoryUpdates implements RepositoryUpdates
    {
        private StatusEventListener statusEventListener;

        public StatusRepositoryUpdates(final StatusEventListener statusEventListener)
        {
            this.statusEventListener = statusEventListener;
        }

        @Override
        public void onNumberOfConnectionsChanged(final int newNumberOfConnections)
        {
            statusEventListener.onEvent(new NumberOfConnectionsChanged(newNumberOfConnections));
        }
    }
}
