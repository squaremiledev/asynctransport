package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.concurrent.TimeUnit;

import org.HdrHistogram.Histogram;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.MutableBoolean;
import org.agrona.collections.MutableLong;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

class SourcingConnectionApplication implements ConnectionApplication
{
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

    @Override
    public void onEvent(final ConnectionEvent event)
    {
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
                    throw new IllegalStateException();
                }
                stoppedMeasuringNanos.set(nanoTime());
                isDone.set(true);
            }
        }
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
