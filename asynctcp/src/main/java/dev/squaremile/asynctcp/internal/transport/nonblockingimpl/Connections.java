package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.nio.channels.SelectionKey;
import java.util.function.Consumer;

import org.agrona.collections.Long2ObjectHashMap;


import dev.squaremile.asynctcp.api.transport.app.ApplicationLifecycle;
import dev.squaremile.asynctcp.api.transport.app.OnDuty;
import dev.squaremile.asynctcp.internal.transport.domain.StatusEventListener;
import dev.squaremile.asynctcp.internal.transport.domain.connection.Connection;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionRepository;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionState;

public class Connections implements ApplicationLifecycle, AutoCloseable, OnDuty
{
    public final Long2ObjectHashMap<SelectionKey> selectionKeyByConnectionId;
    public final Long2ObjectHashMap<ConnectionState> lastUpdatedConnectionStateKeyByConnectionId;
    public final ConnectionRepository connectionRepository;
    private final Consumer<Connection> connectionWork = Connection::work;
    private final Consumer<Connection> connectionOnStart = Connection::onStart;
    private final Consumer<Connection> connectionOnStop = Connection::onStop;

    public Connections(final StatusEventListener statusEventListener)
    {
        this.selectionKeyByConnectionId = new Long2ObjectHashMap<>();
        this.lastUpdatedConnectionStateKeyByConnectionId = new Long2ObjectHashMap<>();
        this.connectionRepository = new ConnectionRepository(new ConnectionRepository.StatusRepositoryUpdates(statusEventListener));
    }

    private static void updateSelectionKeyInterest(final ConnectionState state, final SelectionKey key)
    {
        switch (state)
        {
            case NO_OUTSTANDING_DATA:
                if ((key.interestOps() & SelectionKey.OP_WRITE) != 0)
                {
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                }
                break;
            case DATA_TO_SEND_BUFFERED:
                if ((key.interestOps() & SelectionKey.OP_WRITE) == 0)
                {
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                }
                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                break;
            case CLOSED:
                key.cancel();
                key.attach(null);
                break;
        }
    }

    public void updateBasedOnState(final Connection connection)
    {
        ConnectionState lastConnectionState = lastUpdatedConnectionStateKeyByConnectionId.get(connection.connectionId());
        if (lastConnectionState == null || lastConnectionState != connection.state())
        {
            updateSelectionKeyInterest(connection.state(), getSelectionKey(connection.connectionId()));
            lastUpdatedConnectionStateKeyByConnectionId.put(connection.connectionId(), connection.state());
            if (connection.state() == ConnectionState.CLOSED)
            {
                lastUpdatedConnectionStateKeyByConnectionId.remove(connection.connectionId());
            }
        }
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

    @Override
    public void onStart()
    {
        connectionRepository.forEach(connectionOnStart);
    }

    @Override
    public void onStop()
    {
        connectionRepository.forEach(connectionOnStop);
    }
}
