package com.michaelszymczak.sample.sockets.support;

import com.michaelszymczak.sample.sockets.api.events.NumberOfConnectionsChanged;
import com.michaelszymczak.sample.sockets.api.events.StatusEvent;

public class TearDown
{

    public static void closeCleanly(final TestableTransport<?> transport, final SampleClients clients, final EventsSpy<StatusEvent> statusEventsSpy)
    {
        clients.close();
        if (statusEventsSpy.contains(NumberOfConnectionsChanged.class) && statusEventsSpy.last(NumberOfConnectionsChanged.class).newNumberOfConnections() > 0)
        {
            transport.workUntil(() -> statusEventsSpy.last(NumberOfConnectionsChanged.class).newNumberOfConnections() == 0);
        }
        transport.close();
    }
}
