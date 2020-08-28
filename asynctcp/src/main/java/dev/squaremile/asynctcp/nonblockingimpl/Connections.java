package dev.squaremile.asynctcp.nonblockingimpl;

import java.nio.channels.SelectionKey;

import dev.squaremile.asynctcp.domain.api.events.StatusEventListener;
import dev.squaremile.asynctcp.domain.connection.Connection;
import dev.squaremile.asynctcp.domain.connection.ConnectionRepository;

import org.agrona.collections.Long2ObjectHashMap;

public class Connections implements AutoCloseable
{
    public final Long2ObjectHashMap<SelectionKey> selectionKeyByConnectionId;
    public final ConnectionRepository connectionRepository;

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
    public void close() throws Exception
    {
        selectionKeyByConnectionId.values().forEach(SelectionKey::cancel);
        selectionKeyByConnectionId.clear();
        connectionRepository.close();
    }

    public Connection get(final long connectionId)
    {
        return connectionRepository.findByConnectionId(connectionId);
    }
}
