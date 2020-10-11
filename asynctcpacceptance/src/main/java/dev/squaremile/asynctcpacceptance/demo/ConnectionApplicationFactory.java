package dev.squaremile.asynctcpacceptance.demo;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public interface ConnectionApplicationFactory
{
    ConnectionApplication create(ConnectionTransport connectionTransport, ConnectionId connectionId);
}
