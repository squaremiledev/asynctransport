package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.api.transport.app.ConnectionApplication;
import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.asynctcp.api.transport.values.ConnectionId;

public interface ConnectionApplicationFactory
{
    static ConnectionApplicationFactory onCreate(final ConnectionApplicationFactory connectionApplicationFactory)
    {
        return connectionApplicationFactory;
    }

    static ConnectionApplicationFactory onStart(final OnStartConnectionApplicationFactory onStartConnectionApplicationFactory)
    {
        return (connectionTransport, connectionId) -> new OnStartConnectionApplication(connectionTransport, connectionId, onStartConnectionApplicationFactory);
    }

    static ConnectionApplicationFactory onEvent(final OnEventConnectionApplicationFactory onEventConnectionApplicationFactory)
    {
        return (connectionTransport, connectionId) -> new OnEventConnectionApplication(connectionTransport, onEventConnectionApplicationFactory);
    }

    ConnectionApplication create(ConnectionTransport connectionTransport, ConnectionId connectionId);
}
