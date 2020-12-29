package dev.squaremile.transport.casestudy.marketmaking.domain;

public interface Security extends MarketMessage
{
    long midPrice();

    long lastUpdateTime();

    long lastPriceChange();
}
