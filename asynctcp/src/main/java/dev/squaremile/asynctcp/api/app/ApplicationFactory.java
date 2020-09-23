package dev.squaremile.asynctcp.api.app;

public interface ApplicationFactory
{
    Application create(Transport transport);
}
