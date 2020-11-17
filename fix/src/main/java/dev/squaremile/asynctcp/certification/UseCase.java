package dev.squaremile.asynctcp.certification;

import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;

public interface UseCase
{
    ConnectionApplicationFactory fakeAppFactory();
}
