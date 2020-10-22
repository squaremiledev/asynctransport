package dev.squaremile.asynctcpacceptance;

import java.util.concurrent.TimeUnit;

import org.HdrHistogram.Histogram;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.MutableBoolean;
import org.agrona.collections.MutableLong;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.CommandFailed;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.fixedLengthDelineation;
import static java.lang.Integer.parseInt;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class SourcingConnectionApplication implements ConnectionApplication
{
    private final ConnectionId connectionId;
    private final ConnectionTransport connectionTransport;
    private final Runnable onRoundTripComplete;
    private final int total;
    private final int warmUp;
    private final MutableLong startedMeasuringNanos;
    private final MutableLong stoppedMeasuringNanos;
    private final MutableLong sentFirstTimeNanos = new MutableLong();
    private final MutableBoolean isDone;
    private final Histogram histogram;
    private final long messageDelayNs;
    int timesSent = 0;
    int timesReceived = 0;

    public SourcingConnectionApplication(
            final ConnectionId connectionId,
            final ConnectionTransport connectionTransport,
            final Runnable onRoundTripComplete,
            final int total,
            final int warmUp,
            final MutableLong startedMeasuringNanos,
            final MutableLong stoppedMeasuringNanos,
            final MutableBoolean isDone,
            final Histogram histogram,
            final int sendingRatePerSecond
    )
    {
        this.connectionId = new ConnectionIdValue(connectionId);
        this.connectionTransport = connectionTransport;
        this.onRoundTripComplete = onRoundTripComplete;
        this.total = total;
        this.warmUp = warmUp;
        this.startedMeasuringNanos = startedMeasuringNanos;
        this.stoppedMeasuringNanos = stoppedMeasuringNanos;
        this.isDone = isDone;
        this.histogram = histogram;
        this.messageDelayNs = sendingRatePerSecond == -1 ? -1 : TimeUnit.SECONDS.toNanos(1) / sendingRatePerSecond;
    }

    public static void main(String[] args)
    {
        if (args.length != 5)
        {
            System.out.println("Usage: remoteHost remotePort sendingRatePerSecond warmUpMessages measuredMessages");
            System.out.println("e.g. localhost 8889 48000 400000 4000000");
            return;
        }
        String remoteHost = args[0];
        int remotePort = parseInt(args[1]);
        int sendingRatePerSecond = parseInt(args[2]);
        int warmUpMessages = parseInt(args[3]);
        int measuredMessages = parseInt(args[4]);
        System.out.printf(
                "Starting with remoteHost %s, remotePort %d, sendingRatePerSecond %d, warmUpMessages %d , measuredMessages %d%n",
                remoteHost, remotePort, sendingRatePerSecond, warmUpMessages, measuredMessages
        );
        start(remoteHost, remotePort, sendingRatePerSecond, warmUpMessages, measuredMessages);
    }

    public static void start(final String remoteHost, final int remotePort, final int sendingRatePerSecond, final int warmUp, final int measuredMessages)
    {
        final Histogram histogram = new Histogram(TimeUnit.SECONDS.toNanos(10), 3);
        final MutableLong startedNanos = new MutableLong(-1);
        final MutableLong stoppedNanos = new MutableLong(-1);
        final MutableLong completeRoundTrips = new MutableLong(0);
        final MutableBoolean isDone = new MutableBoolean(false);
        final int total = warmUp + measuredMessages;
        final ApplicationOnDuty source = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
                "source",
                transport -> new ConnectingApplication(
                        transport,
                        remoteHost,
                        remotePort,
                        fixedLengthDelineation(16),
                        (connectionTransport, connectionId) -> new SourcingConnectionApplication(
                                connectionId,
                                connectionTransport,
                                completeRoundTrips::incrementAndGet,
                                total,
                                warmUp,
                                startedNanos,
                                stoppedNanos,
                                isDone,
                                histogram,
                                sendingRatePerSecond
                        )
                )
        );

        source.onStart();
        while (!isDone.get())
        {
            source.work();
        }
        source.onStop();

        final long transactions = completeRoundTrips.get();
        final long messagesExchanged = transactions * 2;
        final long tookMs = NANOSECONDS.toMillis(stoppedNanos.get() - startedNanos.get());
        final long _msgps = messagesExchanged * 1000L / tookMs;
        final long _trps = transactions * 1000L / tookMs;

        //histogram.outputPercentileDistribution(System.out, 1.0);
        System.out.println();
        System.out.print("Exchanged " + messagesExchanged + " messages ");
        System.out.print("at a rate of " + _msgps + " messages per second ");
        System.out.print(" which took " + tookMs + " milliseconds");
        System.out.println();
        System.out.print("Performed " + transactions + " transactions ");
        System.out.print("at a rate of " + _trps + " transactions per second ");
        System.out.print(" which took " + tookMs + " milliseconds");
        System.out.println();
        System.out.println("mean is               " + histogram.getMean() + " microseconds for a round trip");
        System.out.println("99th percentile is    " + histogram.getValueAtPercentile(99) + " microseconds for a round trip");
        System.out.println("99.9th percentile is  " + histogram.getValueAtPercentile(99.9) + " microseconds for a round trip");
        System.out.println("99.99th percentile is " + histogram.getValueAtPercentile(99.99) + " microseconds for a round trip");
        System.out.println("worst is              " + histogram.getMaxValue() + " microseconds for a round trip");
        System.out.println("std deviation is      " + String.format("%.5g", histogram.getStdDeviation()) + " microseconds");
    }

    @Override
    public ConnectionId connectionId()
    {
        return connectionId;
    }

    @Override
    public void onStart()
    {
        if (messageDelayNs == -1)
        {
            send(nanoTime());
        }
    }

    @Override
    public void work()
    {
        if (messageDelayNs >= 0 && timesSent < total)
        {
            if (timesSent == 0)
            {
                send(nanoTime());
            }
            else
            {
                long nowNs = nanoTime();
                long expectedTimestampNsToSendThisMessage = sentFirstTimeNanos.get() + timesSent * messageDelayNs;
                if (nowNs >= expectedTimestampNsToSendThisMessage)
                {
                    send(expectedTimestampNsToSendThisMessage);
                }
            }
        }
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof CommandFailed)
        {
            throw new IllegalStateException(((CommandFailed)event).details());
        }
        if (event instanceof MessageReceived)
        {
            timesReceived++;
            if (messageDelayNs == -1 && timesSent < total)
            {
                send(nanoTime());
            }
            MessageReceived messageReceived = (MessageReceived)event;
            long sendTimeNs = messageReceived.buffer().getLong(messageReceived.offset());
            long responseTimeNs = messageReceived.buffer().getLong(messageReceived.offset() + 8);
            long now = nanoTime();
            if (timesReceived == warmUp)
            {
                startedMeasuringNanos.set(nanoTime());
            }
            if (timesReceived > warmUp)
            {
                onRoundTripComplete.run();
                onResults(timesReceived, NANOSECONDS.toMicros(sendTimeNs), NANOSECONDS.toMicros(responseTimeNs), NANOSECONDS.toMicros(now));
            }
            if (timesReceived == total)
            {
                if (timesReceived != timesSent)
                {
                    throw new IllegalStateException("Received " + timesReceived + " times, but sent " + timesSent + " times");
                }
                stoppedMeasuringNanos.set(nanoTime());
                isDone.set(true);
            }
        }
    }

    private void onResults(final int timesSent, final long sendTimeUs, final long responseTimeUs, final long nowUs)
    {
        long roundTripTimeUs = nowUs - sendTimeUs;
        histogram.recordValue(roundTripTimeUs);
    }

    private void send(final long supposedSendingTimestampNs)
    {
        SendMessage message = connectionTransport.command(SendMessage.class);
        MutableDirectBuffer buffer = message.prepare();
        buffer.putLong(message.offset(), supposedSendingTimestampNs);
        buffer.putLong(message.offset() + 8, -1L);
        message.commit(16);
        connectionTransport.handle(message);
        timesSent++;
        if (timesSent == 1)
        {
            sentFirstTimeNanos.set(nanoTime());
        }
    }
}
