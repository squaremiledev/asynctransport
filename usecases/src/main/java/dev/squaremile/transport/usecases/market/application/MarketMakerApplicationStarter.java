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
    private final MarketMakerTransportApplication.MarketMakerApplicationFactory marketMakerApplicationFactory;

    private MarketMakerApplication marketMakerApplication;

    public MarketMakerApplicationStarter(final String remoteHost, final int remotePort, final MarketMakerTransportApplication.MarketMakerApplicationFactory marketMakerApplicationFactory)
    {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.marketMakerApplicationFactory = marketMakerApplicationFactory;
    }

    public TransportApplicationOnDuty startTransport(final Runnable runUntilReady, final int timeoutMs)
    {
        if (marketMakerApplication() != null)
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
                                    marketMakerApplication = marketMakerApplicationFactory.create(new MarketMakerPublisher(connectionTransport));
                                    return new MarketMakerTransportApplication(marketMakerApplication);
                                }
                        )
        );
        application.onStart();
        long startTime = currentTimeMillis();
        while (marketMakerApplication == null && currentTimeMillis() < startTime + timeoutMs)
        {
            application.work();
            runUntilReady.run();
        }
        if (marketMakerApplication == null)
        {
            throw new IllegalStateException("Unable to satisfy condition within " + timeoutMs + "ms.");
        }
        return application;
    }

    public MarketMakerApplication marketMakerApplication()
    {
        return marketMakerApplication;
    }
}
