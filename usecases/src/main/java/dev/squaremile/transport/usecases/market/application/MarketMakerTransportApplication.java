package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;

class MarketMakerTransportApplication implements ConnectionApplication
{
    private final MarketMakerApplication marketMakerApplication;
    private final Serialization serialization = new Serialization();

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
            MarketMessage marketMessage = serialization.decode(messageReceived.buffer(), messageReceived.offset());
            if (marketMessage instanceof FirmPrice)
            {
                marketMakerApplication.onFirmPriceUpdated((FirmPrice)marketMessage);
            }
        }
    }

    interface MarketMakerApplicationFactory
    {
        MarketMakerApplication create(MarketMakerPublisher publisher);
    }

}
