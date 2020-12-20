package dev.squaremile.transport.usecases.market.domain;

@FunctionalInterface
public interface MidPriceUpdate
{
    Security newMidPrice(long currentTime, TrackedSecurity security);
}
