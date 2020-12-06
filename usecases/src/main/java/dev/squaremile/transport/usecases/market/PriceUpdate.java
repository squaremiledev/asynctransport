package dev.squaremile.transport.usecases.market;

@FunctionalInterface
interface PriceUpdate
{
    long newPrice(long currentTime, Security security);
}
