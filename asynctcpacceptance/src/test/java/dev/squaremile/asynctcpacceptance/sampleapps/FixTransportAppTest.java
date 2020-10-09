package dev.squaremile.asynctcpacceptance.sampleapps;

import org.agrona.collections.MutableInteger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcpacceptance.sampleapps.fix.FixTransportApp;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static java.util.concurrent.locks.LockSupport.parkNanos;

public class FixTransportAppTest
{
    private static final int NUMBER_OF_CYCLES = 100_000;
    private final MutableInteger messageCount = new MutableInteger();

    @Test
    void shouldExchangeMessages()
    {

        Application application = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
                "heartBeating", transport -> new FixTransportApp(transport, freePort(), messageCount::increment));
        application.onStart();

        long before = System.currentTimeMillis();
        for (int i = 0; i < NUMBER_OF_CYCLES; i++)
        {
            application.work();
        }
        long after = System.currentTimeMillis();
        long messagesPerSecond = messageCount.get() * 1000 / (after - before);
        System.out.println(String.format("Exchanged %d messages at the rate of %d msg/s", messageCount.get(), messagesPerSecond));

        application.onStop();

        for (int i = 0; i < 100; i++)
        {
            application.work();
            parkNanos(1000);
        }

        assertThat(messagesPerSecond).isGreaterThan(10_000);
    }
}
