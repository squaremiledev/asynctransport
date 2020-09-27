package dev.squaremile.asynctcp.api;

import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.setup.TransportApplication;

public interface TransportApplicationFactory
{
    TransportApplication create(final String role, ApplicationFactory applicationFactory);
}
