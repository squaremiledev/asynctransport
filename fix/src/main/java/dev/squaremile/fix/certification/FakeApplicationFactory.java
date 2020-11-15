package dev.squaremile.fix.certification;

import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public interface FakeApplicationFactory extends ConnectionApplicationFactory
{
    @Override
    FakeApplication create(ConnectionTransport connectionTransport, ConnectionId connectionId);
}
