package dev.squaremile.transport.casestudy.marketmaking.domain;

@FunctionalInterface
public interface TickListener
{
    TickListener IGNORE = security ->
    {
    };

    void onTick(Security security);
}
