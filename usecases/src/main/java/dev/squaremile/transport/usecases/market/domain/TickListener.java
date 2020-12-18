package dev.squaremile.transport.usecases.market.domain;

@FunctionalInterface
public interface TickListener
{
    TickListener NO_LISTENER = security ->
    {

    };

    void onTick(Security security);
}
