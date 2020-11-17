package dev.squaremile.asynctcp.certification;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public interface UseCaseApplicationFactory<UseCase>
{
    ConnectionApplication create(
            final ConnectionTransport connectionTransport,
            final ConnectionId connectionId,
            final UseCase useCase
    );
}
