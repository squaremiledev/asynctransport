package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;

class MarketMakerTransportApplication implements ConnectionApplication
{
    private final MarketMakerApplication marketMakerApplication;

    interface MarketMakerApplicationFactory
    {
        MarketMakerApplication create(MarketMakerPublisher publisher);
    }

    public MarketMakerTransportApplication(final MarketMakerApplication marketMakerApplication)
    {
        this.marketMakerApplication = marketMakerApplication;
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)event;
            long value = messageReceived.buffer().getLong(messageReceived.offset());
            marketMakerApplication.onFirmPriceUpdated(value);
        }
    }

}
