package dev.squaremile.asynctcpacceptance;

import java.util.function.Function;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public interface ConnectionApplicationProvider
{
    ConnectionApplication onStart(final ConnectionId connectionId);

    ConnectionApplication onEvent(final ConnectionEvent event);

    static ConnectionApplicationProvider connectionApplication(final Function<ConnectionId, ConnectionApplication> applicationFactory)
    {
        return new ConnectionApplicationProvider()
        {
            @Override
            public ConnectionApplication onStart(final ConnectionId connectionId)
            {
                return applicationFactory.apply(connectionId);
            }

            @Override
            public ConnectionApplication onEvent(final ConnectionEvent event)
            {
                return null;
            }
        };
    }
}
