package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.HdrHistogram.Histogram;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.MutableBoolean;
import org.agrona.collections.MutableLong;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.agrona.concurrent.ringbuffer.RingBufferDescriptor.TRAILER_LENGTH;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcpacceptance.demo.ApplicationLifecycle;
import dev.squaremile.asynctcpacceptance.demo.SingleLocalConnectionDemoApplication;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class RoundTripTimeTest
{
    private static final int WARM_UP = 10_000;
    private static final int TIMES_MEASURED = 100_000;
    private static final int TOTAL = WARM_UP + TIMES_MEASURED;
    private static final Histogram HISTOGRAM = new Histogram(TimeUnit.SECONDS.toNanos(10), 3);
    private final ApplicationLifecycle applicationLifecycle = new ApplicationLifecycle();
    private final MutableBoolean isDone = new MutableBoolean(false);
    private final MutableLong startedNanos = new MutableLong(-1);
    private final MutableLong stoppedNanos = new MutableLong(-1);
    private final MutableLong completeRoundTrips = new MutableLong(0);
    private final Consumer<String> log = s ->
    {
    };

    static Stream<Function<ApplicationFactory, Application>> applicationSuppliers()
    {
        final TransportApplicationFactory transportApplicationFactory = new AsyncTcp().transportAppFactory(NON_PROD_GRADE);
        return Stream.of(
                applicationFactory -> transportApplicationFactory.create(
                        "sameStack",
                        applicationFactory
                ),
                applicationFactory -> transportApplicationFactory.create(
                        "viaRingBuffers",
                        new OneToOneRingBuffer(new UnsafeBuffer(new byte[1024 * 1024 + TRAILER_LENGTH])),
                        new OneToOneRingBuffer(new UnsafeBuffer(new byte[1024 * 1024 + TRAILER_LENGTH])),
                        applicationFactory
                )
        );
    }

    @ParameterizedTest
    @MethodSource("applicationSuppliers")
    void measureRoundTripTime(final Function<ApplicationFactory, Application> applicationSupplier)
    {
        Application app = applicationSupplier.apply(transport -> new SingleLocalConnectionDemoApplication(
                transport,
                fixedLengthDelineation(2 * 8),
                applicationLifecycle,
                log,
                freePort(),
                (connectionTransport, connectionId) -> new SourcingApplication(connectionTransport, completeRoundTrips::incrementAndGet),
                EchoApplication::new
        ));

        app.onStart();

        while (!isDone.get())
        {
            app.work();
        }

        app.onStop();

        while (applicationLifecycle.isUp())
        {
            app.work();
        }

        long messagesExchanged = completeRoundTrips.get() * 2;
        long tookMs = NANOSECONDS.toMillis(stoppedNanos.get() - startedNanos.get());
        long _msgps = messagesExchanged * 1000L / tookMs;

        HISTOGRAM.outputPercentileDistribution(System.out, 1.0);
        System.out.println();
        System.out.print("Exchanged " + messagesExchanged + " messages ");
        System.out.print("at a rate of " + _msgps + " messages per second ");
        System.out.print(" which took " + MILLISECONDS.toSeconds(tookMs) + " seconds");
        System.out.println();
        System.out.println("99.99th percentile is " + HISTOGRAM.getValueAtPercentile(99.99) + " microseconds");
    }

    private static class EchoApplication implements ConnectionApplication
    {
        private final ConnectionTransport connectionTransport;

        public EchoApplication(final ConnectionTransport connectionTransport, final ConnectionId connectionId)
        {
            this.connectionTransport = connectionTransport;
        }

        @Override
        public void onEvent(final ConnectionEvent event)
        {
            if (event instanceof MessageReceived)
            {
                MessageReceived messageReceived = (MessageReceived)event;
                long sendTimeNs = messageReceived.buffer().getLong(messageReceived.offset());
                SendMessage message = connectionTransport.command(SendMessage.class);
                MutableDirectBuffer buffer = message.prepare();
                buffer.putLong(message.offset(), sendTimeNs);
                buffer.putLong(message.offset() + 8, nanoTime());
                message.commit(16);
                connectionTransport.handle(message);
            }
        }
    }

    private class SourcingApplication implements ConnectionApplication
    {
        private final ConnectionTransport connectionTransport;
        int timesSent = 0;
        int timesReceived = 0;
        private final Runnable onRoundTripComplete;

        public SourcingApplication(final ConnectionTransport connectionTransport, final Runnable onRoundTripComplete)
        {
            this.connectionTransport = connectionTransport;
            this.onRoundTripComplete = onRoundTripComplete;
        }

        @Override
        public void work()
        {
            if (timesSent < TOTAL)
            {
                send();
                timesSent++;
            }
        }

        @Override
        public void onEvent(final ConnectionEvent event)
        {
            if (event instanceof MessageReceived)
            {
                timesReceived++;
                MessageReceived messageReceived = (MessageReceived)event;
                long sendTimeNs = messageReceived.buffer().getLong(messageReceived.offset());
                long responseTimeNs = messageReceived.buffer().getLong(messageReceived.offset() + 8);
                long now = nanoTime();
                if (timesReceived == WARM_UP)
                {
                    startedNanos.set(nanoTime());
                }
                if (timesReceived > WARM_UP)
                {
                    onRoundTripComplete.run();
                    onResults(timesReceived, NANOSECONDS.toMicros(sendTimeNs), NANOSECONDS.toMicros(responseTimeNs), NANOSECONDS.toMicros(now));
                }
                if (timesReceived == TOTAL)
                {
                    if (timesReceived != timesSent)
                    {
                        throw new IllegalStateException();
                    }
                    stoppedNanos.set(nanoTime());
                    isDone.set(true);
                }
            }
        }

        private void onResults(final int timesSent, final long sendTimeUs, final long responseTimeUs, final long nowUs)
        {
            long roundTripTimeUs = nowUs - sendTimeUs;
            HISTOGRAM.recordValue(roundTripTimeUs);
        }

        private void send()
        {
            SendMessage message = connectionTransport.command(SendMessage.class);
            MutableDirectBuffer buffer = message.prepare();
            buffer.putLong(message.offset(), nanoTime());
            buffer.putLong(message.offset() + 8, -1L);
            message.commit(16);
            connectionTransport.handle(message);
        }
    }
}
