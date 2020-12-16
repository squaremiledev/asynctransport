package dev.squaremile.transport.usecases.market.application;

import java.util.function.BooleanSupplier;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.wiring.ConnectingApplication;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;
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
                        (connectionTransport, connectionId) -> event ->
                        {

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

    public void updatePrice(final FirmPrice firmPrice)
    {
        // TODO: handle market updates instead
        acknowledgedPriceUpdatesCount++;
    }

    public int acknowledgedPriceUpdatesCount()
    {
        return acknowledgedPriceUpdatesCount;
    }
}
