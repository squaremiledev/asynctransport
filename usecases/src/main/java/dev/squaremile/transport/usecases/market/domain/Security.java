package dev.squaremile.transport.usecases.market.domain;

public interface Security extends MarketMessage
{
    long midPrice();

    long lastUpdateTime();

    long lastPriceChange();
}
