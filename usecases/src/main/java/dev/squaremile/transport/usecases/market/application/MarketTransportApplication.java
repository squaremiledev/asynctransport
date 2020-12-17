package dev.squaremile.transport.usecases.market.application;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;

import static java.lang.System.currentTimeMillis;

class MarketTransportApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;
    private final FirmPrice decodedFirmPrice = FirmPrice.createNoPrice();
    private final Clock clock;

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
            decodedFirmPrice.update(
                    messageReceived.buffer().getLong(messageReceived.offset()),
                    messageReceived.buffer().getLong(messageReceived.offset() + Long.BYTES),
                    messageReceived.buffer().getLong(messageReceived.offset() + Long.BYTES * 2),
                    messageReceived.buffer().getInt(messageReceived.offset() + Long.BYTES * 3),
                    messageReceived.buffer().getLong(messageReceived.offset() + Long.BYTES * 3 + Integer.BYTES),
                    messageReceived.buffer().getInt(messageReceived.offset() + Long.BYTES * 4 + Integer.BYTES)
            );

            SendMessage sendMessage = connectionTransport.command(SendMessage.class);
            MutableDirectBuffer buffer = sendMessage.prepare();
            buffer.putLong(sendMessage.offset(), decodedFirmPrice.correlationId());
            buffer.putLong(sendMessage.offset() + Long.BYTES, clock.currentTimeMs());
            buffer.putLong(sendMessage.offset() + Long.BYTES * 2, decodedFirmPrice.bidPrice());
            buffer.putLong(sendMessage.offset() + Long.BYTES * 3, decodedFirmPrice.bidQuantity());
            buffer.putLong(sendMessage.offset() + Long.BYTES * 3 + Integer.BYTES, decodedFirmPrice.askPrice());
            buffer.putLong(sendMessage.offset() + Long.BYTES * 4 + Integer.BYTES, decodedFirmPrice.askQuantity());
            sendMessage.commit(Long.BYTES * 4 + Integer.BYTES * 2);
            connectionTransport.handle(sendMessage);
        }
    }

    @Override
    public void work()
    {
        clock.updateCurrentTimeMs(currentTimeMillis());
    }
}
