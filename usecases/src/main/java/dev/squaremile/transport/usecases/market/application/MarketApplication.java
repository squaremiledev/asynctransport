package dev.squaremile.transport.usecases.market.application;

import java.util.function.BooleanSupplier;

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

public class MarketApplication
{
    private final int port;
    private final Runnable onReady;
    private final Runnable onConnected;
    private final MutableBoolean startedListening = new MutableBoolean(false);
    private final MutableBoolean connected = new MutableBoolean(false);
    private TransportApplicationOnDuty transportApplication;

    public MarketApplication(final int port)
    {
        this.port = port;
        this.onReady = () -> startedListening.set(true);
        this.onConnected = () -> connected.set(true);
    }

    public TransportApplicationOnDuty startTransport(final BooleanSupplier returnWhen, final int timeoutMs)
    {
        if (transportApplication != null)
        {
            throw new IllegalStateException("Application already started");
        }
        final TransportApplicationOnDuty application = new AsyncTcp().create("marketApp", 1024, SerializedMessageListener.NO_OP, transport -> new ListeningApplication(
                transport,
                lengthBasedDelineation(SHORT_LITTLE_ENDIAN_FIELD, 0, 0),
                port,
                event ->
                {
                    if (event instanceof StartedListening)
                    {
                        onReady.run();
                    }
                },
                onStart((connectionTransport, connectionId) ->
                        {
                            onConnected.run();
                            return event ->
                            {
                            };
                        })
        ));
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

    public boolean startedListening()
    {
        if (transportApplication != null)
        {
            transportApplication.work();
        }
        return startedListening.get();
    }

    public boolean acceptedMarketMakerConnection()
    {
        if (transportApplication != null)
        {
            transportApplication.work();
        }
        return connected.get();
    }
}
