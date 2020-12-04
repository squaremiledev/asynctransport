package dev.squaremile.asynctcp.api.certification;

import java.util.Optional;

import org.agrona.collections.MutableBoolean;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.api.wiring.OnEventConnectionApplicationFactory;
import dev.squaremile.asynctcp.serialization.api.SerializedMessageListener;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

public class Certification
{
    private final int buffersSize;
    private final Delineation delineation;
    private final UseCases useCases;

    public Certification(final int buffersSize, final Delineation delineation, final UseCases useCases)
    {
        this.buffersSize = buffersSize;
        this.delineation = delineation;
        this.useCases = useCases;
    }


    public ApplicationOnDuty start(final int port, final SerializedMessageListener acceptorMessageListener)
    {
        final MutableBoolean readyToAcceptConnections = new MutableBoolean(false);
        final ApplicationOnDuty acceptor = new AsyncTcp().create(
                "acceptor",
                buffersSize,
                acceptorMessageListener,
                new AcceptorApplicationFactory(
                        port,
                        () -> readyToAcceptConnections.set(true),
                        delineation,
                        (connectionTransport, event) ->
                        {
                            if (event instanceof MessageReceived)
                            {
                                return useCases.useCase((MessageReceived)event).map(useCase -> useCase.fakeAppFactory().create(connectionTransport, event));
                            }
                            return Optional.empty();
                        }
                )
        );
        acceptor.onStart();
        while (!readyToAcceptConnections.get())
        {
            acceptor.work();
        }
        return acceptor;
    }

    private static class AcceptorApplicationFactory implements TransportApplicationOnDutyFactory
    {
        private final int port;
        private final Runnable onStartedListening;
        private final OnEventConnectionApplicationFactory onEventConnectionApplicationFactory;
        private final Delineation delineation;

        public AcceptorApplicationFactory(
                final int port,
                final Runnable onStartedListening,
                final Delineation delineation,
                final OnEventConnectionApplicationFactory onEventConnectionApplicationFactory
        )
        {
            this.port = port;
            this.onStartedListening = onStartedListening;
            this.onEventConnectionApplicationFactory = onEventConnectionApplicationFactory;
            this.delineation = delineation;
        }

        @Override
        public TransportApplicationOnDuty create(final Transport transport)
        {
            return new ListeningApplication(
                    transport,
                    delineation,
                    port,
                    event ->
                    {
                        if (event instanceof StartedListening)
                        {
                            onStartedListening.run();
                        }
                    },
                    ConnectionApplicationFactory.onEvent(onEventConnectionApplicationFactory)
            );
        }

    }
}
