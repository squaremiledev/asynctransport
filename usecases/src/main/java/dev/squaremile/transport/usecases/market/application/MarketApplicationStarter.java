package dev.squaremile.transport.usecases.market.application;

import org.agrona.collections.MutableBoolean;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.serialization.api.SerializedMessageListener;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;

import static dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory.onStart;
import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.lengthBasedDelineation;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.Type.SHORT_LITTLE_ENDIAN_FIELD;
import static java.lang.System.currentTimeMillis;

public class MarketApplicationStarter
{
    private final int port;
    private final Clock clock;
    private TransportApplicationOnDuty transportApplication;
    private MarketTransportApplication marketTransportApplication;

    public MarketApplicationStarter(final int port, final Clock clock)
    {
        this.port = port;
        this.clock = clock;
    }

    public TransportApplicationOnDuty startTransport(final int timeoutMs)
    {
        if (transportApplication != null)
        {
            throw new IllegalStateException("Application already started");
        }
        final MutableBoolean startedListening = new MutableBoolean(false);
        final TransportApplicationOnDuty application = new AsyncTcp().create("marketApp", 1024, SerializedMessageListener.NO_OP, transport ->
                new ListeningApplication(
                        transport,
                        lengthBasedDelineation(SHORT_LITTLE_ENDIAN_FIELD, 0, 0),
                        port,
                        event ->
                        {
                            if (event instanceof StartedListening)
                            {
                                startedListening.set(true);
                            }
                        },
                        onStart((connectionTransport, connectionId) ->
                                {
                                    marketTransportApplication = new MarketTransportApplication(connectionTransport, clock);
                                    return marketTransportApplication;
                                })
                ));
        application.onStart();
        long startTime = currentTimeMillis();
        while (!startedListening.get() && currentTimeMillis() < startTime + timeoutMs)
        {
            application.work();
        }
        if (!startedListening.get())
        {
            throw new IllegalStateException("Unable to satisfy condition within " + timeoutMs + "ms.");
        }
        this.transportApplication = application;
        return application;
    }

    public MarketTransportApplication application()
    {
        return marketTransportApplication;
    }

}
