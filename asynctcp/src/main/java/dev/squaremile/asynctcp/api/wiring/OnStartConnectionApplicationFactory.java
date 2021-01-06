package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.api.transport.app.ConnectionApplication;
import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.asynctcp.api.transport.values.ConnectionId;

public interface OnStartConnectionApplicationFactory
{
    ConnectionApplication createOnStart(final ConnectionTransport connectionTransport, final ConnectionId connectionId);
}
