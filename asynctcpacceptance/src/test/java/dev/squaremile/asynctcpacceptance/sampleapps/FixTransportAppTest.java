package dev.squaremile.asynctcpacceptance.sampleapps;

import org.agrona.collections.MutableLong;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcpacceptance.demo.ApplicationLifecycle;
import dev.squaremile.asynctcpacceptance.demo.SingleLocalConnectionDemoApplication;
import dev.squaremile.asynctcpacceptance.sampleapps.fix.RejectLogOn;
import dev.squaremile.asynctcpacceptance.sampleapps.fix.SendLogOn;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.FIX_MESSAGES;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

public class FixTransportAppTest
{
    private static final int TOTAL_MESSAGES_TO_RECEIVE = 1_000_000;
    private final MutableLong messageCount = new MutableLong();
    private final ApplicationLifecycle lifecycleListener = new ApplicationLifecycle();

    @Test
    void shouldExchangeMessages()
    {
        ApplicationOnDuty application = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create("heartBeating", transport ->
                new SingleLocalConnectionDemoApplication(
                        transport,
                        FIX_MESSAGES.type,
                        lifecycleListener,
                        s ->
                        {
                        },
                        freePort(),
                        (connectionTransport, connectionId) -> new RejectLogOn(connectionTransport, messageCount::increment, connectionId),
                        (connectionTransport, connectionId) -> new SendLogOn(connectionTransport, messageCount::increment, connectionId, TOTAL_MESSAGES_TO_RECEIVE / 2)
                ));
        application.onStart();
        while (!lifecycleListener.isUp())
        {
            application.work();
        }

        long before = System.currentTimeMillis();
        while (messageCount.get() < TOTAL_MESSAGES_TO_RECEIVE)
        {
            application.work();
        }
        long after = System.currentTimeMillis();

        application.onStop();

        while (lifecycleListener.isUp())
        {
            application.work();
        }

        long messagesPerSecond = messageCount.get() * 1000 / (after - before);
        System.out.println(String.format("Exchanged %d messages at the rate of %d msg/s", messageCount.get(), messagesPerSecond));
        assertThat(messagesPerSecond).isGreaterThan(10_000);
    }
}
