package dev.squaremile.transport.usecases.market.domain;

public interface Security
{
    long midPrice();

    long lastUpdateTime();

    long lastPriceChange();
}