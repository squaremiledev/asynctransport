package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

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
        return (connectionTransport, connectionId) -> new OnEventConnectionApplication(connectionTransport, connectionId, onEventConnectionApplicationFactory);
    }

    ConnectionApplication create(ConnectionTransport connectionTransport, ConnectionId connectionId);
}
