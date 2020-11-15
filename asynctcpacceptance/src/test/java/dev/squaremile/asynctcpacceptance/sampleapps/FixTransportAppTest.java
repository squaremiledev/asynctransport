package dev.squaremile.asynctcpacceptance.sampleapps;

import org.agrona.collections.MutableBoolean;
import org.agrona.collections.MutableLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.api.wiring.ConnectingApplication;
import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.fixtures.TimingExtension;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcpacceptance.demo.ApplicationLifecycle;
import dev.squaremile.asynctcpacceptance.sampleapps.fix.RejectLogOn;
import dev.squaremile.asynctcpacceptance.sampleapps.fix.SendLogOn;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.fixMessage;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

@ExtendWith(TimingExtension.class)
public class FixTransportAppTest
{
    private static final int TOTAL_MESSAGES_TO_RECEIVE = 1_000;
    private final MutableLong messageCount = new MutableLong();
    private final ApplicationLifecycle lifecycleListener = new ApplicationLifecycle();
    private final TransportApplicationFactory transportApplicationFactory = new AsyncTcp().transportAppFactory(NON_PROD_GRADE);

    @Test
    void shouldExchangeMessages()
    {
        final MutableBoolean startedListening = new MutableBoolean(false);
        ConnectionApplicationFactory acceptorFactory = (connectionTransport, connectionId) -> new RejectLogOn(connectionTransport, messageCount::increment, connectionId);
        ConnectionApplicationFactory initiatorFactory = (connectionTransport, connectionId) -> new SendLogOn(
                connectionTransport,
                messageCount::increment,
                connectionId,
                TOTAL_MESSAGES_TO_RECEIVE / 2
        );
        int port = freePort();
        ApplicationOnDuty acceptorApplication = transportApplicationFactory.createSharedStack(
                "acceptor",
                transport -> new ListeningApplication(transport, fixMessage(), port, new EventListener()
                {
                    @Override
                    public void onEvent(final Event event)
                    {
                        if (event instanceof StartedListening)
                        {
                            startedListening.set(true);
                        }
                    }
                }, acceptorFactory)
        );
        ApplicationOnDuty initiatorApplication = transportApplicationFactory.createSharedStack(
                "initiator",
                transport -> new ConnectingApplication(transport, "localhost", port, fixMessage(), initiatorFactory)
        );
//        final ApplicationOnDuty initiatorApplication = transportApplicationFactory.createSharedStack("heartBeating", transport ->
//                new SingleLocalConnectionDemoApplication(
//                        transport,
//                        fixMessage(),
//                        lifecycleListener,
//                        s ->
//                        {
//                        },
//                        port,
//                        new ConnectionApplicationFactory()
//                        {
//                            @Override
//                            public ConnectionApplication create(
//                                    final ConnectionTransport connectionTransport, final ConnectionId connectionId
//                            )
//                            {
//                                return new ConnectionApplication()
//                                {
//                                    @Override
//                                    public ConnectionId connectionId()
//                                    {
//                                        return connectionId;
//                                    }
//
//                                    @Override
//                                    public void onEvent(final ConnectionEvent event)
//                                    {
//
//                                    }
//                                };
//                            }
//                        },
//                        initiatorFactory
//                ));
        acceptorApplication.onStart();
        while (!startedListening.get())
        {
            acceptorApplication.work();
        }
        initiatorApplication.onStart();
        long before = System.currentTimeMillis();
        while (messageCount.get() < TOTAL_MESSAGES_TO_RECEIVE)
        {
            acceptorApplication.work();
            initiatorApplication.work();
        }
        long after = System.currentTimeMillis();

        acceptorApplication.onStop();
        initiatorApplication.onStop();

        while (lifecycleListener.isUp())
        {
            acceptorApplication.work();
            initiatorApplication.work();
        }

        long messagesPerSecond = messageCount.get() * 1000 / (after - before);
        System.out.println(String.format("Exchanged %d messages at the rate of %d msg/s", messageCount.get(), messagesPerSecond));
        assertThat(messagesPerSecond).isGreaterThan(10_000);
    }
}
