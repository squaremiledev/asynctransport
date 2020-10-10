package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.HdrHistogram.Histogram;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.MutableBoolean;
import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcpacceptance.demo.ApplicationLifecycle;
import dev.squaremile.asynctcpacceptance.demo.SingleLocalConnectionDemoApplication;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class RoundTripTimeTest
{
    private static final Histogram HISTOGRAM = new Histogram(TimeUnit.SECONDS.toNanos(10), 3);
    private final ApplicationLifecycle applicationLifecycle = new ApplicationLifecycle();
    private final MutableBoolean isDone = new MutableBoolean(false);
    private final Consumer<String> log = s ->
    {
    };

    @Test
    void measureRoundTripTime()
    {
        Application app = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
                "roundTripTime",
                transport -> new SingleLocalConnectionDemoApplication(
                        transport,
                        fixedLengthDelineation(2 * 8),
                        applicationLifecycle,
                        log,
                        freePort(),
                        (connectionTransport, connectionId) -> new ConnectionApplication()
                        {
                            final static int WARM_UP = 10_000;
                            final static int TIMES_MEASURED = 100_000;
                            final static int TOTAL = WARM_UP + TIMES_MEASURED;
                            int timesSent = 0;

                            @Override
                            public void onStart()
                            {
                                send();
                            }

                            @Override
                            public void onEvent(final ConnectionEvent event)
                            {
                                if (event instanceof MessageReceived)
                                {
                                    MessageReceived messageReceived = (MessageReceived)event;
                                    long sendTimeNs = messageReceived.buffer().getLong(messageReceived.offset());
                                    long responseTimeNs = messageReceived.buffer().getLong(messageReceived.offset() + 8);
                                    long now = nanoTime();
                                    if (timesSent > WARM_UP)
                                    {
                                        onResults(timesSent, NANOSECONDS.toMicros(sendTimeNs), NANOSECONDS.toMicros(responseTimeNs), NANOSECONDS.toMicros(now));
                                    }

                                    if (timesSent < TOTAL)
                                    {
                                        send();
                                    }
                                    else
                                    {
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
                                timesSent++;
                            }
                        },
                        (connectionTransport, connectionId) -> event ->
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
                )
        );

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

        HISTOGRAM.outputPercentileDistribution(System.out, 1.0);
    }
}
