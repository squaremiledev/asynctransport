package dev.squaremile.transport.usecases.market;

@FunctionalInterface
interface MidPriceUpdate
{
    long newMidPrice(long currentTime, Security security);
}
