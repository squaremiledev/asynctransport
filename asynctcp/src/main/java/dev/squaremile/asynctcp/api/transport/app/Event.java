package dev.squaremile.asynctcp.api.transport.app;

public interface Event
{
    Event copy();

    default boolean occursInSteadyState()
    {
        return true;
    }
}
