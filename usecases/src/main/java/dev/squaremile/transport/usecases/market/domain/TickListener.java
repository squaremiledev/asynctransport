package dev.squaremile.transport.usecases.market.domain;

@FunctionalInterface
interface TickListener
{
    void onTick(Security security);
}
