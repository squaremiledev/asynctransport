package dev.squaremile.tcpprobe;

import java.util.concurrent.TimeUnit;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class Probe
{
    private final Measurements measurements;
    private final SelectiveResponseRequest selectiveResponseRequest;
    private final int messagesToSend;
    private final int respondToEveryNthRequest;
    private final long messageDelayNs;
    private final Metadata metadata = new Metadata();

    private long messagesSentCount = 0;
    private long messagesReceivedCount = 0;
    private long awaitingResponsesInFlightCount = 0;
    private long startedSendingTimestampNanos = Long.MIN_VALUE;

    private Probe(final String description, final int totalNumberOfMessagesToSend, final int skippedResponses, final int respondToEveryNthRequest, final int sendingRatePerSecond)
    {
        int expectedResponses = totalNumberOfMessagesToSend / respondToEveryNthRequest;
        if (skippedResponses >= expectedResponses)
        {
            throw new IllegalArgumentException("All " + expectedResponses + " responses would be skipped");
        }

        this.messagesToSend = totalNumberOfMessagesToSend;
        this.respondToEveryNthRequest = respondToEveryNthRequest;
        this.measurements = new Measurements(description, skippedResponses + 1);
        this.selectiveResponseRequest = new SelectiveResponseRequest(totalNumberOfMessagesToSend, respondToEveryNthRequest);
        this.messageDelayNs = TimeUnit.SECONDS.toNanos(1) / sendingRatePerSecond;
    }

    public static Configuration probe(final String description)
    {
        return new Configuration(description);
    }

    public int onTime(final long nowNs, final MutableDirectBuffer outboundBuffer, final int outboundOffset)
    {
        final long sendTimestampNs;
        if (startedSendingTimestampNanos == Long.MIN_VALUE)
        {
            sendTimestampNs = nowNs;
            startedSendingTimestampNanos = nowNs;
        }
        else
        {
            sendTimestampNs = startedSendingTimestampNanos + messagesSentCount * messageDelayNs;
        }

        if (messagesSentCount >= messagesToSend || nowNs < sendTimestampNs)
        {
            return 0;
        }

        metadata.wrap(outboundBuffer, outboundOffset).clear().options().respond(selectiveResponseRequest.shouldRespond(messagesSentCount));
        metadata.originalTimestampNs(sendTimestampNs).correlationId(messagesSentCount);
        if (selectiveResponseRequest.shouldRespond(messagesSentCount))
        {
            awaitingResponsesInFlightCount++;
        }
        messagesSentCount++;
        return metadata.length();
    }

    public void onMessageReceived(final DirectBuffer buffer, final int offset, final long currentTimeNanos)
    {
        metadata.wrap(buffer, offset);
        awaitingResponsesInFlightCount--;
        if (((messagesReceivedCount) * respondToEveryNthRequest) != metadata.correlationId())
        {
            throw new IllegalStateException("A mismatch detected");
        }
        messagesReceivedCount++;
        measurements.onMessageReceived(messagesSentCount, messagesReceivedCount, metadata.originalTimestampNs(), currentTimeNanos);
    }

    public boolean hasReceivedAll()
    {
        boolean receivedAll = selectiveResponseRequest.receivedLast(messagesReceivedCount);
        if (receivedAll && awaitingResponsesInFlightCount != 0)
        {
            throw new IllegalStateException("At this point we should have received all expected responses, " +
                                            "but " + awaitingResponsesInFlightCount + " are still in flight");
        }
        return receivedAll;
    }

    public Measurements measurementsCopy()
    {
        if (!hasReceivedAll())
        {
            throw new IllegalStateException("The report is available after probing is done");
        }
        return measurements.copy();
    }

    public static class Configuration
    {
        private final String description;
        private int totalNumberOfMessagesToSend = Integer.MIN_VALUE;
        private int skippedResponses = Integer.MIN_VALUE;
        private int respondToEveryNthRequest = Integer.MIN_VALUE;
        private int sendingRatePerSecond = Integer.MIN_VALUE;

        private Configuration(final String description)
        {
            this.description = description;
        }

        public Configuration totalNumberOfMessagesToSend(final int totalNumberOfMessagesToSend)
        {
            this.totalNumberOfMessagesToSend = totalNumberOfMessagesToSend;
            return this;
        }

        public Configuration skippedResponses(final int skippedResponses)
        {
            this.skippedResponses = skippedResponses;
            return this;
        }

        public Configuration respondToEveryNthRequest(final int respondToEveryNthRequest)
        {
            this.respondToEveryNthRequest = respondToEveryNthRequest;
            return this;
        }

        public Configuration sendingRatePerSecond(final int sendingRatePerSecond)
        {
            this.sendingRatePerSecond = sendingRatePerSecond;
            return this;
        }

        public Probe createProbe()
        {
            if (
                    totalNumberOfMessagesToSend == Integer.MIN_VALUE ||
                    skippedResponses == Integer.MIN_VALUE ||
                    respondToEveryNthRequest == Integer.MIN_VALUE ||
                    sendingRatePerSecond == Integer.MIN_VALUE)
            {
                throw new IllegalArgumentException("Not all configuration hs been set");
            }
            return new Probe(description, totalNumberOfMessagesToSend, skippedResponses, respondToEveryNthRequest, sendingRatePerSecond);
        }
    }
}
