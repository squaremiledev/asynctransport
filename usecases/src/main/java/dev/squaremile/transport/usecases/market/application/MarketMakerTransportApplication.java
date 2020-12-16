package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;

class MarketMakerTransportApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;
    private long inFlightFirmPriceUpdateUpdateTime;
    private int acknowledgedPriceUpdatesCount = 0;

    public MarketMakerTransportApplication(final ConnectionTransport connectionTransport)
    {
        this.connectionTransport = connectionTransport;
    }

    public void updatePrice(final FirmPrice firmPrice)
    {
        inFlightFirmPriceUpdateUpdateTime = firmPrice.updateTime();
        // TODO: encode the actual message instead
        SendMessage sendMessage = connectionTransport.command(SendMessage.class);
        sendMessage.prepare().putLong(sendMessage.offset(), inFlightFirmPriceUpdateUpdateTime);
        sendMessage.commit(Long.BYTES);
        connectionTransport.handle(sendMessage);
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)event;
            long value = messageReceived.buffer().getLong(messageReceived.offset());
            if (value == inFlightFirmPriceUpdateUpdateTime)
            {
                acknowledgedPriceUpdatesCount++;
            }
        }
    }

    public int acknowledgedPriceUpdatesCount()
    {
        return acknowledgedPriceUpdatesCount;
    }
}
