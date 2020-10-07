package dev.squaremile.asynctcp.api;

import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;

public interface TransportApplicationFactory
{
    Application create(final String role, ApplicationFactory applicationFactory);
}
