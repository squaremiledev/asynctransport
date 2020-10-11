package dev.squaremile.asynctcpacceptance.sampleapps;

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
    private final MutableLong startedNanos;
    private final MutableLong stoppedNanos;
    private final MutableBoolean isDone;
    private final Histogram histogram;
    int timesSent = 0;
    int timesReceived = 0;

    public SourcingConnectionApplication(
            final ConnectionTransport connectionTransport,
            final Runnable onRoundTripComplete,
            final int total,
            final int warmUp,
            final MutableLong startedNanos,
            final MutableLong stoppedNanos,
            final MutableBoolean isDone,
            final Histogram histogram
    )
    {
        this.connectionTransport = connectionTransport;
        this.onRoundTripComplete = onRoundTripComplete;
        this.total = total;
        this.warmUp = warmUp;
        this.startedNanos = startedNanos;
        this.stoppedNanos = stoppedNanos;
        this.isDone = isDone;
        this.histogram = histogram;
    }

    @Override
    public void work()
    {
        if (timesSent < total)
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
            if (timesReceived == warmUp)
            {
                startedNanos.set(nanoTime());
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
                stoppedNanos.set(nanoTime());
                isDone.set(true);
            }
        }
    }

    private void onResults(final int timesSent, final long sendTimeUs, final long responseTimeUs, final long nowUs)
    {
        long roundTripTimeUs = nowUs - sendTimeUs;
        histogram.recordValue(roundTripTimeUs);
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
