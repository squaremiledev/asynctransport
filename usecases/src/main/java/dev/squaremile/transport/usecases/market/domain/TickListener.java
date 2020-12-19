package dev.squaremile.transport.usecases.market.domain;

@FunctionalInterface
public interface TickListener
{
    TickListener IGNORE = security ->
    {
    };

    void onTick(Security security);
}
