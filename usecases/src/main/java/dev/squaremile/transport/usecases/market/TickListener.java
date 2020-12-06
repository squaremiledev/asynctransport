package dev.squaremile.transport.usecases.market;

@FunctionalInterface
interface TickListener
{
    void onTick(Security security);
}
