package dev.squaremile.transport.usecases.market.application;

import org.agrona.collections.MutableBoolean;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.wiring.ConnectingApplication;
import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.lengthBasedDelineation;
import static dev.squaremile.asynctcp.serialization.api.SerializedMessageListener.NO_OP;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.Type.SHORT_LITTLE_ENDIAN_FIELD;
import static dev.squaremile.transport.usecases.market.domain.CurrentTime.currentTime;
import static dev.squaremile.transport.usecases.market.domain.CurrentTime.timeFromMs;

public class InitiatorStarter
{
    private final int remotePort;
    private final String remoteHost;
    private final ConnectionApplicationFactory connectionApplicationFactory;
    private final MutableBoolean started = new MutableBoolean();
    private final String role;

    public InitiatorStarter(
            final String role,
            final String remoteHost,
            final int remotePort,
            final ConnectionApplicationFactory connectionApplicationFactory
    )
    {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.connectionApplicationFactory = connectionApplicationFactory;
        this.role = role;
    }

    public TransportApplicationOnDuty startTransport(final Runnable runUntilReady, final int timeoutMs)
    {
        if (started.get())
        {
            throw new IllegalStateException("Application already started");
        }
        final TransportApplicationOnDuty application = new AsyncTcp().create(
                role,
                16 * 1024,
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
        long startTime = currentTime();
        while (!this.started.get() && currentTime() < startTime + timeFromMs(timeoutMs))
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
