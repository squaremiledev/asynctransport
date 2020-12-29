package dev.squaremile.transport.casestudy.marketmaking.domain;

@FunctionalInterface
public interface MidPriceUpdate
{
    Security newMidPrice(long currentTime, TrackedSecurity security);
}
