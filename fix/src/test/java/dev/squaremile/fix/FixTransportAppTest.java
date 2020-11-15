package dev.squaremile.fix;

import org.agrona.collections.MutableBoolean;
import org.agrona.collections.MutableLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.api.wiring.ConnectingApplication;
import dev.squaremile.asynctcp.fixtures.TimingExtension;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.fixMessage;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

@ExtendWith(TimingExtension.class)
public class FixTransportAppTest
{
    private static final int TOTAL_MESSAGES_TO_RECEIVE = 1_000;
    private final MutableLong messageCount = new MutableLong();
    private final TransportApplicationFactory transportApplicationFactory = new AsyncTcp().transportAppFactory(NON_PROD_GRADE);
    private final MutableBoolean startedListening = new MutableBoolean(false);
    private final int port = freePort();

    @Test
    void shouldExchangeMessages()
    {
        final ApplicationOnDuty initiator = transportApplicationFactory.createSharedStack("initiator", transport ->
                new ConnectingApplication(
                        transport,
                        "localhost",
                        port,
                        fixMessage(),
                        (connectionTransport, connectionId) -> new SendLogOn(
                                connectionTransport,
                                messageCount::increment,
                                connectionId,
                                TOTAL_MESSAGES_TO_RECEIVE / 2
                        )
                )
        );
        final ApplicationOnDuty acceptor = transportApplicationFactory.createSharedStack(
                "acceptor",
                new FixAcceptorFactory(
                        port,
                        () -> startedListening.set(true),
                        (connectionTransport, connectionId, fixVersion, username) -> new RejectLogOn(
                                connectionTransport,
                                messageCount::increment,
                                connectionId
                        )
                )
        );
        acceptor.onStart();
        while (!startedListening.get())
        {
            acceptor.work();
        }
        initiator.onStart();
        long before = System.currentTimeMillis();
        while (messageCount.get() < TOTAL_MESSAGES_TO_RECEIVE)
        {
            acceptor.work();
            initiator.work();
        }
        long after = System.currentTimeMillis();

        acceptor.onStop();
        initiator.onStop();

        long messagesPerSecond = messageCount.get() * 1000 / (after - before);
        System.out.printf("Exchanged %d messages at the rate of %d msg/s%n", messageCount.get(), messagesPerSecond);
        assertThat(messagesPerSecond).isGreaterThan(10_000);
    }

}
