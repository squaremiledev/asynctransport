package dev.squaremile.transport.usecases.market.domain;

@FunctionalInterface
interface MidPriceUpdate
{
    long newMidPrice(long currentTime, Security security);
}
