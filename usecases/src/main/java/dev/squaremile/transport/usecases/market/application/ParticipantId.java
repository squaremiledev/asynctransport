package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public class ParticipantId
{
    static int fromConnectionId(final ConnectionId connectionId)
    {
        return (int)connectionId.connectionId();
    }

    static long toConnectionId(final int marketParticipantId)
    {
        return marketParticipantId;
    }
}
