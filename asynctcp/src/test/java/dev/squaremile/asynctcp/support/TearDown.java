package dev.squaremile.asynctcp.support;

import dev.squaremile.asynctcp.domain.api.events.NumberOfConnectionsChanged;

public class TearDown
{

    public static void closeCleanly(final TransportUnderTest transport)
    {
        transport.close();
        StatusEventsSpy statusEventsSpy = transport.statusEvents();
        if (statusEventsSpy.contains(NumberOfConnectionsChanged.class) && statusEventsSpy.last(NumberOfConnectionsChanged.class).newNumberOfConnections() > 0)
        {
            System.out.println("statusEventsSpy.all() = " + statusEventsSpy.all());
            transport.workUntil(() -> statusEventsSpy.last(NumberOfConnectionsChanged.class).newNumberOfConnections() == 0);
        }
    }
}
