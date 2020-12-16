package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.wiring.ConnectingApplication;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.lengthBasedDelineation;
import static dev.squaremile.asynctcp.serialization.api.SerializedMessageListener.NO_OP;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.Type.SHORT_LITTLE_ENDIAN_FIELD;
import static java.lang.System.currentTimeMillis;

public class MarketMakerApplicationStarter
{

    private final int remotePort;
    private final String remoteHost;

    private MarketMakerTransportApplication marketMakerTransportApplication;

    public MarketMakerApplicationStarter(final String remoteHost, final int remotePort)
    {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public TransportApplicationOnDuty startTransport(final Runnable runUntilReady, final int timeoutMs)
    {
        if (marketMakerTransportApplication() != null)
        {
            throw new IllegalStateException("Application already started");
        }
        final TransportApplicationOnDuty application = new AsyncTcp().create(
                "marketMaker",
                1024,
                NO_OP,
                transport ->
                        new ConnectingApplication(
                                transport,
                                remoteHost,
                                remotePort,
                                lengthBasedDelineation(SHORT_LITTLE_ENDIAN_FIELD, 0, 0),
                                (connectionTransport, connectionId) ->
                                {
                                    marketMakerTransportApplication = new MarketMakerTransportApplication(connectionTransport);
                                    return marketMakerTransportApplication;
                                }
                        )
        );
        application.onStart();
        long startTime = currentTimeMillis();
        while (marketMakerTransportApplication == null && currentTimeMillis() < startTime + timeoutMs)
        {
            application.work();
            runUntilReady.run();
        }
        if (marketMakerTransportApplication == null)
        {
            throw new IllegalStateException("Unable to satisfy condition within " + timeoutMs + "ms.");
        }
        return application;
    }

    public MarketMakerTransportApplication marketMakerTransportApplication()
    {
        return marketMakerTransportApplication;
    }
}
