package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public interface OnStartConnectionApplicationFactory
{
    ConnectionApplication createOnStart(final ConnectionTransport connectionTransport, final ConnectionId connectionId);
}
