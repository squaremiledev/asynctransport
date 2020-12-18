package dev.squaremile.transport.usecases.market.application;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;

import static java.lang.System.currentTimeMillis;

class MarketTransportApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;
    private final FirmPrice firmPriceResponse = FirmPrice.createNoPrice();
    private final Clock clock;
    private final Serialization serialization = new Serialization();

    public MarketTransportApplication(final ConnectionTransport connectionTransport, final Clock clock)
    {
        this.connectionTransport = connectionTransport;
        this.clock = clock;
    }

    @Override
    public void onEvent(final ConnectionEvent connectionEvent)
    {
        clock.updateCurrentTimeMs(currentTimeMillis());
        if (connectionEvent instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)connectionEvent;
            MarketMessage marketMessage = serialization.decode(messageReceived.buffer(), messageReceived.offset());
            if (marketMessage instanceof FirmPrice)
            {
                FirmPrice firmPrice = (FirmPrice)marketMessage;
                firmPriceResponse.update(clock.currentTimeMs(), firmPrice);
                SendMessage sendMessage = connectionTransport.command(SendMessage.class);
                MutableDirectBuffer buffer = sendMessage.prepare();
                int encodedLength = serialization.encode(firmPriceResponse, buffer, sendMessage.offset());
                sendMessage.commit(encodedLength);
                connectionTransport.handle(sendMessage);
            }
        }
    }

    @Override
    public void work()
    {
        clock.updateCurrentTimeMs(currentTimeMillis());
    }
}
