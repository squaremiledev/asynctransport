package dev.squaremile.asynctcp.certification;

import java.util.Optional;

import org.agrona.collections.MutableBoolean;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.api.wiring.OnEventConnectionApplicationFactory;
import dev.squaremile.asynctcp.serialization.internal.SerializedMessageListener;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;

public class Certification<UseCase>
{
    private final int buffersSize;
    private final Delineation delineation;
    private final SerializedMessageListener acceptorMessageLog;
    private final Resolver<UseCase> resolver;
    private final UseCaseApplicationFactory<UseCase> useCaseApplicationFactory;

    public Certification(
            final int buffersSize,
            final Delineation delineation,
            final SerializedMessageListener acceptorMessageLog,
            final Resolver<UseCase> resolver,
            final UseCaseApplicationFactory<UseCase> useCaseApplicationFactory
    )
    {
        this.buffersSize = buffersSize;
        this.delineation = delineation;
        this.acceptorMessageLog = acceptorMessageLog;
        this.resolver = resolver;
        this.useCaseApplicationFactory = useCaseApplicationFactory;
    }


    public ApplicationOnDuty start(final int port)
    {
        final MutableBoolean readyToAcceptConnections = new MutableBoolean(false);
        final ApplicationOnDuty acceptor = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
                "acceptor",
                buffersSize,
                acceptorMessageLog,
                new AcceptorApplicationFactory(
                        port,
                        () -> readyToAcceptConnections.set(true),
                        delineation,
                        new OnMetadataConnectionApplicationFactory<>(useCaseApplicationFactory, resolver)
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

    private static class OnMetadataConnectionApplicationFactory<Metadata> implements OnEventConnectionApplicationFactory
    {
        private final UseCaseApplicationFactory<Metadata> useCaseApplicationFactory;
        private final Resolver<Metadata> resolver;

        public OnMetadataConnectionApplicationFactory(final UseCaseApplicationFactory<Metadata> useCaseApplicationFactory, final Resolver<Metadata> resolver)
        {
            this.useCaseApplicationFactory = useCaseApplicationFactory;
            this.resolver = resolver;
        }

        @Override
        public Optional<ConnectionApplication> createOnEvent(final ConnectionTransport connectionTransport, final ConnectionEvent event)
        {
            if (event instanceof MessageReceived)
            {
                return resolver
                        .useCase((MessageReceived)event)
                        .map(metadata -> useCaseApplicationFactory.create(connectionTransport, event, metadata));
            }
            return Optional.empty();
        }

    }
}
