package com.michaelszymczak.sample.sockets.connection;

import java.util.HashMap;
import java.util.Map;

import com.michaelszymczak.sample.sockets.nio.Resources;

public class ConnectionRepository implements AutoCloseable
{
    private final Map<Long, ConnectionAggregate> connections = new HashMap<>();

    public ConnectionRepository()
    {
    }

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
}
