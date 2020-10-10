package dev.squaremile.asynctcp.transport.api.app;

public interface Event
{
    Event copy();

    default boolean occursInSteadyState()
    {
        return true;
    }
}
