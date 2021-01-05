package dev.squaremile.asynctcp.transport.internal.nonblockingimpl;

import java.nio.channels.SelectionKey;
import java.util.function.Consumer;

import org.agrona.collections.Long2ObjectHashMap;


import dev.squaremile.asynctcp.transport.api.app.OnDuty;
import dev.squaremile.asynctcp.transport.internal.domain.StatusEventListener;
import dev.squaremile.asynctcp.transport.internal.domain.connection.Connection;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionRepository;

public class Connections implements AutoCloseable, OnDuty
{
    public final Long2ObjectHashMap<SelectionKey> selectionKeyByConnectionId;
    public final ConnectionRepository connectionRepository;
    private final Consumer<Connection> connectionWork = OnDuty::work;

    public Connections(final StatusEventListener statusEventListener)
    {
        this.selectionKeyByConnectionId = new Long2ObjectHashMap<>();
        this.connectionRepository = new ConnectionRepository(new ConnectionRepository.StatusRepositoryUpdates(statusEventListener));
    }

    public void add(final Connection connection, final SelectionKey selectionKey)
    {
        connectionRepository.add(connection);
        selectionKeyByConnectionId.put(connection.connectionId(), selectionKey);
    }

    public void remove(long connectionId)
    {
        selectionKeyByConnectionId.remove(connectionId);
        connectionRepository.removeById(connectionId);
    }

    public boolean contains(long connectionId)
    {
        return connectionRepository.contains(connectionId);
    }

    public SelectionKey getSelectionKey(final long connectionId)
    {
        return selectionKeyByConnectionId.get(connectionId);
    }

    @Override
    public void close()
    {
        selectionKeyByConnectionId.values().forEach(SelectionKey::cancel);
        selectionKeyByConnectionId.clear();
        connectionRepository.close();
    }

    public Connection get(final long connectionId)
    {
        return connectionRepository.findByConnectionId(connectionId);
    }

    @Override
    public void work()
    {
        connectionRepository.forEach(connectionWork);
    }
}
