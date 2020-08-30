package dev.squaremile.asynctcp.application;

import dev.squaremile.asynctcp.domain.api.Transport;

public interface ApplicationFactory
{
    Application create(Transport transport);
}
