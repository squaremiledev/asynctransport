package dev.squaremile.asynctcp.api.certification;

import java.util.Optional;

import org.agrona.collections.MutableBoolean;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.api.wiring.OnEventConnectionApplicationFactory;
import dev.squaremile.asynctcp.serialization.internal.SerializedMessageListener;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;

public class Certification<T extends UseCase>
{
    private final int buffersSize;
    private final Delineation delineation;
    private final Resolver<T> resolver;

    public Certification(final int buffersSize, final Delineation delineation, final Resolver<T> resolver)
    {
        this.buffersSize = buffersSize;
        this.delineation = delineation;
        this.resolver = resolver;
    }


    public ApplicationOnDuty start(final int port, final SerializedMessageListener acceptorMessageListener)
    {
        final MutableBoolean readyToAcceptConnections = new MutableBoolean(false);
        final ApplicationOnDuty acceptor = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
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
                                return resolver.useCase((MessageReceived)event).map(useCase -> useCase.fakeAppFactory().create(connectionTransport, event));
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

    private static class AcceptorApplicationFactory implements ApplicationFactory
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
        public EventDrivenApplication create(final Transport transport)
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
