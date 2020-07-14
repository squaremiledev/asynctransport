package com.michaelszymczak.sample.sockets.connection;

import java.util.HashMap;
import java.util.Map;

import com.michaelszymczak.sample.sockets.nio.Resources;

public class ConnectionRepository implements AutoCloseable
{
    // TODO: autoboxing
    private final Map<Long, ConnectionAggregate> connections = new HashMap<>();

    public void add(final ConnectionAggregate connection)
    {
        connections.put(connection.connectionId(), connection);
    }

    public int size()
    {
        return connections.size();
    }

    public ConnectionAggregate findByConnectionId(final long connectionId)
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
        // TODO - remember to close when implementing removal (with void return value) or clear
        connections.values().forEach(Resources::close);
    }

    public void removeById(final long connectionId)
    {
        if (!connections.containsKey(connectionId))
        {
            return;
        }
        final ConnectionAggregate connection = connections.get(connectionId);
        if (!connection.isClosed())
        {
            throw new IllegalStateException("Connection must be closed before it's removed");
        }
        connections.remove(connectionId);
    }
}
