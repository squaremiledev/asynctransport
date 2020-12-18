package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.transport.usecases.market.domain.MarketMessage;

public interface BusinessApplication
{
    void onMessage(MarketMessage marketMessage);

    void onPeriodicWakeUp();
}
