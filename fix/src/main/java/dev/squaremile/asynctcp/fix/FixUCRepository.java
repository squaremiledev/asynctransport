package dev.squaremile.asynctcp.fix;

import java.util.HashMap;
import java.util.Map;


import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.certification.UseCaseApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public class FixUCRepository implements UseCaseApplicationFactory<FixMetadata>
{
    private final Map<String, Map<String, ConnectionApplicationFactory>> useCaseForVersionAndUser = new HashMap<>(5);

    public FixUCRepository(FixUCFake... useCaseFakes)
    {
        for (final FixUCFake useCaseFake : useCaseFakes)
        {
            final Map<String, ConnectionApplicationFactory> fakesForFixVersion = useCaseForVersionAndUser.computeIfAbsent(useCaseFake.fixVersion(), (key) -> new HashMap<>(10));
            if (fakesForFixVersion.containsKey(useCaseFake.username()))
            {
                throw new IllegalArgumentException("Entry " + useCaseFake + " has been already configured");
            }
            fakesForFixVersion.put(useCaseFake.username(), useCaseFake.applicationFactory());
        }
    }

    @Override
    public ConnectionApplication create(
            final ConnectionTransport connectionTransport,
            final ConnectionId connectionId,
            final FixMetadata metadata
    )
    {
        if (!useCaseForVersionAndUser.containsKey(metadata.fixVersion()))
        {
            return new IgnoreAll();
        }
        final Map<String, ConnectionApplicationFactory> useCaseForUser = useCaseForVersionAndUser.get(metadata.fixVersion());
        if (!useCaseForUser.containsKey(metadata.username()))
        {
            return new IgnoreAll();
        }
        return useCaseForUser.get(metadata.username()).create(connectionTransport, connectionId);
    }
}
