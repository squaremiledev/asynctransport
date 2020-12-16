package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

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
            long correlationId = messageReceived.buffer().getLong(messageReceived.offset());
            marketMakerApplication.onFirmPriceUpdated(correlationId);
        }
    }

}
