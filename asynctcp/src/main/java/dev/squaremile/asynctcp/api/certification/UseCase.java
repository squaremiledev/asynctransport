package dev.squaremile.asynctcp.api.certification;

import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;

public interface UseCase
{
    ConnectionApplicationFactory fakeAppFactory();
}
