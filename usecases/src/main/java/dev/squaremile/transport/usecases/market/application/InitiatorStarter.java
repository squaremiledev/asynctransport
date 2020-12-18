package dev.squaremile.transport.usecases.market.application;

import org.agrona.collections.MutableBoolean;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.wiring.ConnectingApplication;
import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.lengthBasedDelineation;
import static dev.squaremile.asynctcp.serialization.api.SerializedMessageListener.NO_OP;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.Type.SHORT_LITTLE_ENDIAN_FIELD;
import static java.lang.System.currentTimeMillis;

public class InitiatorStarter
{
    private final int remotePort;
    private final String remoteHost;
    private final ConnectionApplicationFactory connectionApplicationFactory;
    private final MutableBoolean started = new MutableBoolean();

    public InitiatorStarter(
            final String remoteHost,
            final int remotePort,
            final ConnectionApplicationFactory connectionApplicationFactory
    )
    {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.connectionApplicationFactory = connectionApplicationFactory;
    }

    public TransportApplicationOnDuty startTransport(final Runnable runUntilReady, final int timeoutMs)
    {
        if (started.get())
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
                                    this.started.set(true);
                                    return connectionApplicationFactory.create(connectionTransport, connectionId);
                                }
                        )
        );
        application.onStart();
        long startTime = currentTimeMillis();
        while (!this.started.get() && currentTimeMillis() < startTime + timeoutMs)
        {
            application.work();
            runUntilReady.run();
        }
        if (!this.started.get())
        {
            throw new IllegalStateException("Unable to satisfy condition within " + timeoutMs + "ms.");
        }
        return application;
    }
}
