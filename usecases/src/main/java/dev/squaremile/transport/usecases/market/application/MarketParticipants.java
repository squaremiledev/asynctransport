package dev.squaremile.transport.usecases.market.application;

import java.util.function.LongConsumer;

import org.agrona.collections.LongArrayList;


import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public class MarketParticipants
{
    private final LongArrayList connectionIds = new LongArrayList();

    int fromConnectionId(final ConnectionId connectionId)
    {
        // TODO: some better mapping
        return (int)connectionId.connectionId();
    }

    public void forEachConnectedParticipantConnectionId(final LongConsumer action)
    {
        connectionIds.forEachOrderedLong(action);
    }

    public void onConnected(final ConnectionId connectionId)
    {
        connectionIds.add(connectionId.connectionId());
    }

    // TODO: remove on disconnected
}
