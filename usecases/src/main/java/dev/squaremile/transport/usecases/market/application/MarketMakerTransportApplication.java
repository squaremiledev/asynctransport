package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;

class MarketMakerTransportApplication implements ConnectionApplication
{
    private final MarketMakerApplication marketMakerApplication;
    private final FirmPrice decodedFirmPrice = FirmPrice.createNoPrice();

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
            decodedFirmPrice.update(
                    messageReceived.buffer().getLong(messageReceived.offset()),
                    messageReceived.buffer().getLong(messageReceived.offset() + Long.BYTES),
                    messageReceived.buffer().getLong(messageReceived.offset() + Long.BYTES * 2),
                    messageReceived.buffer().getInt(messageReceived.offset() + Long.BYTES * 3),
                    messageReceived.buffer().getLong(messageReceived.offset() + Long.BYTES * 3 + Integer.BYTES),
                    messageReceived.buffer().getInt(messageReceived.offset() + Long.BYTES * 4 + Integer.BYTES)
            );
            marketMakerApplication.onFirmPriceUpdated(decodedFirmPrice);
        }
    }

    interface MarketMakerApplicationFactory
    {
        MarketMakerApplication create(MarketMakerPublisher publisher);
    }

}
