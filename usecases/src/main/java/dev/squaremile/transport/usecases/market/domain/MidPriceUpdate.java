package dev.squaremile.transport.usecases.market.domain;

@FunctionalInterface
public interface MidPriceUpdate
{
    long newMidPrice(long currentTime, Security security);
}
