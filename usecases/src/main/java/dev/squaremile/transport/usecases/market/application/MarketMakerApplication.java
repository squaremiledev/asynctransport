package dev.squaremile.transport.usecases.market.application;

import java.util.function.BooleanSupplier;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.wiring.ConnectingApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.lengthBasedDelineation;
import static dev.squaremile.asynctcp.serialization.api.SerializedMessageListener.NO_OP;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.Type.SHORT_LITTLE_ENDIAN_FIELD;
import static java.lang.System.currentTimeMillis;

public class MarketMakerApplication
{

    private final int remotePort;
    private final String remoteHost;

    private TransportApplicationOnDuty transportApplication;
    private int acknowledgedPriceUpdatesCount = 0;
    private ConnectionTransport connectionTransport;
    private ConnectionIdValue connectionId;
    private long inFlightFirmPriceUpdateUpdateTime;

    public MarketMakerApplication(final String remoteHost, final int remotePort)
    {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public TransportApplicationOnDuty startTransport(final BooleanSupplier returnWhen, final int timeoutMs)
    {
        if (transportApplication != null)
        {
            throw new IllegalStateException("Application already started");
        }
        final TransportApplicationOnDuty application = new AsyncTcp().create(
                "marketMaker",
                1024,
                NO_OP,
                transport -> new ConnectingApplication(
                        transport,
                        remoteHost,
                        remotePort,
                        lengthBasedDelineation(SHORT_LITTLE_ENDIAN_FIELD, 0, 0),
                        (connectionTransport, connectionId) ->
                        {
                            this.connectionTransport = connectionTransport;
                            this.connectionId = new ConnectionIdValue(connectionId);
                            return this::onEvent;
                        }
                )
        );
        application.onStart();
        long startTime = currentTimeMillis();
        while (!returnWhen.getAsBoolean() && currentTimeMillis() < startTime + timeoutMs)
        {
            application.work();
        }
        if (!returnWhen.getAsBoolean())
        {
            throw new IllegalStateException("Unable to satisfy condition within " + timeoutMs + "ms.");
        }
        this.transportApplication = application;
        return application;
    }

    private void onEvent(final ConnectionEvent event)
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

    public void updatePrice(final FirmPrice firmPrice)
    {
        inFlightFirmPriceUpdateUpdateTime = firmPrice.updateTime();
        // TODO: encode the actual message instead
        SendMessage sendMessage = connectionTransport.command(SendMessage.class);
        sendMessage.prepare().putLong(sendMessage.offset(), inFlightFirmPriceUpdateUpdateTime);
        sendMessage.commit(Long.BYTES);
        connectionTransport.handle(sendMessage);
    }

    public int acknowledgedPriceUpdatesCount()
    {
        return acknowledgedPriceUpdatesCount;
    }
}
