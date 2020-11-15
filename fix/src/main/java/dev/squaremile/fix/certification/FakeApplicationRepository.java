package dev.squaremile.fix.certification;

import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public interface FakeApplicationRepository extends FixApplicationFactory
{
    @Override
    FakeApplication create(final ConnectionTransport connectionTransport, final ConnectionId connectionId, final String fixVersion, final String username);
}
