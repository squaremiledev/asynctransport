package dev.squaremile.asynctcp.transport.api.app;

public interface ApplicationFactory
{
    Application create(Transport transport);
}
