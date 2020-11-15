package dev.squaremile.fix.certification;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public interface FixApplicationFactory
{
    ConnectionApplication create(final ConnectionTransport connectionTransport, final ConnectionId connectionId, final String fixVersion, final String username);
}
