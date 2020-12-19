package dev.squaremile.transport.usecases.market.domain;

@FunctionalInterface
public interface TickListener
{
    void onTick(Security security);
}
