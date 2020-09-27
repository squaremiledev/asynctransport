package dev.squaremile.asynctcp.transport.internal.domain.connection;

import org.agrona.CloseHelper;
import org.agrona.collections.Long2ObjectHashMap;


import dev.squaremile.asynctcp.transport.internal.domain.NumberOfConnectionsChanged;
import dev.squaremile.asynctcp.transport.internal.domain.StatusEventListener;

public class ConnectionRepository implements AutoCloseable
{
    private final Long2ObjectHashMap<Connection> connectionsById = new Long2ObjectHashMap<>();
    private final RepositoryUpdates repositoryUpdates;

    public ConnectionRepository(final RepositoryUpdates repositoryUpdates)
    {
        this.repositoryUpdates = repositoryUpdates;
    }

    public void add(final Connection connection)
    {
        if (contains(connection.connectionId()))
        {
            throw new IllegalStateException("Connection " + connection.connectionId() + " already exists");
        }
        connectionsById.put(connection.connectionId(), connection);
        notifyOfNumberOfConnections();
    }

    public int size()
    {
        return connectionsById.size();
    }

    public Connection findByConnectionId(final long connectionId)
    {
        return connectionsById.get(connectionId);
    }

    public boolean contains(final long connectionId)
    {
        return connectionsById.containsKey(connectionId);
    }

    @Override
    public void close()
    {
        connectionsById.values().forEach(CloseHelper::close);
        connectionsById.clear();
        notifyOfNumberOfConnections();
    }

    public void removeById(final long connectionId)
    {
        if (!connectionsById.containsKey(connectionId))
        {
            return;
        }
        final Connection connection = connectionsById.get(connectionId);
        if (connection.state() != ConnectionState.CLOSED)
        {
            throw new IllegalStateException("Connection must be closed before it's removed");
        }
        connectionsById.remove(connectionId);
        notifyOfNumberOfConnections();
    }

    private void notifyOfNumberOfConnections()
    {
        repositoryUpdates.onNumberOfConnectionsChanged(connectionsById.size());
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
