package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;

public interface LazyConnectionApplicationFactory
{
    ConnectionApplication onStart();
}
