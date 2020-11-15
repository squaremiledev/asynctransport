package dev.squaremile.fix.certification;

import java.util.HashMap;
import java.util.Map;


import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public class UseCasesRepository implements FakeApplicationRepository
{
    private final Map<String, Map<String, FakeApplicationFactory>> useCaseForVersionAndUser = new HashMap<>(5);

    public UseCasesRepository(UseCaseFake... useCaseFakes)
    {
        for (final UseCaseFake useCaseFake : useCaseFakes)
        {
            this.useCaseForVersionAndUser
                    .computeIfAbsent(useCaseFake.fixVersion(), (key) -> new HashMap<>(10))
                    .put(useCaseFake.username(), useCaseFake.applicationFactory());
        }
    }

    @Override
    public FakeApplication create(final ConnectionTransport connectionTransport, final ConnectionId connectionId, final String fixVersion, final String username)
    {
        if (!useCaseForVersionAndUser.containsKey(fixVersion))
        {
            return new IgnoreAll();
        }
        final Map<String, FakeApplicationFactory> useCaseForUser = useCaseForVersionAndUser.get(fixVersion);
        if (!useCaseForUser.containsKey(username))
        {
            return new IgnoreAll();
        }
        return useCaseForUser.get(username).create(connectionTransport, connectionId);
    }
}
