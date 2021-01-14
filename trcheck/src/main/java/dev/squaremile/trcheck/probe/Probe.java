package dev.squaremile.trcheck.probe;

import java.util.concurrent.TimeUnit;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;


import static dev.squaremile.trcheck.probe.Metadata.DEFAULT_CORRELATION_ID_OFFSET;
import static dev.squaremile.trcheck.probe.Metadata.DEFAULT_OPTIONS_OFFSET;
import static dev.squaremile.trcheck.probe.Metadata.DEFAULT_SEND_TIME_OFFSET;

public class Probe
{
    private final Measurements measurements;
    private final SelectiveResponseRequest selectiveResponseRequest;
    private final int messagesToSend;
    private final int respondToEveryNthRequest;
    private final long messageDelayNs;
    private final Metadata metadata;

    private long messagesSentCount = 0;
    private long messagesReceivedCount = 0;
    private long awaitingResponsesInFlightCount = 0;
    private long startedSendingTimestampNanos = Long.MIN_VALUE;

    private Probe(
            final String description,
            final Metadata metadata,
            final int totalNumberOfMessagesToSend,
            final int skippedResponses,
            final int respondToEveryNthRequest,
            final int sendingRatePerSecond
    )
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
        this.metadata = metadata;
    }

    public static Configuration probe(final String description)
    {
        return new Configuration(description);
    }

    public boolean onTime(final long nowNs, final MutableDirectBuffer outboundBuffer, final int outboundOffset, final int availableLength)
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
            return false;
        }

        final boolean shouldRespond = selectiveResponseRequest.shouldRespond(messagesSentCount);
        metadata.wrap(outboundBuffer, outboundOffset, availableLength)
                .clear()
                .originalTimestampNs(sendTimestampNs)
                .correlationId(messagesSentCount)
                .options().respond(shouldRespond);

        if (shouldRespond)
        {
            awaitingResponsesInFlightCount++;
        }
        measurements.messagesSentCount(++messagesSentCount);
        return true;
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
        measurements.onMessageReceived(messagesReceivedCount, metadata.originalTimestampNs(), currentTimeNanos);
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

    public void onDataSent(final long totalBytesSentSoFar, final long currentTimeNanos)
    {
        measurements.onDataSent(totalBytesSentSoFar, currentTimeNanos);
    }

    @Override
    public String toString()
    {
        return "Probe{" +
               "measurements=" + measurements +
               ", selectiveResponseRequest=" + selectiveResponseRequest +
               ", messagesToSend=" + messagesToSend +
               ", respondToEveryNthRequest=" + respondToEveryNthRequest +
               ", messageDelayNs=" + messageDelayNs +
               ", metadata=" + metadata +
               ", messagesSentCount=" + messagesSentCount +
               ", messagesReceivedCount=" + messagesReceivedCount +
               ", awaitingResponsesInFlightCount=" + awaitingResponsesInFlightCount +
               ", startedSendingTimestampNanos=" + startedSendingTimestampNanos +
               '}';
    }

    public void onMessageSent()
    {

    }

    public static class Configuration
    {
        private final String description;
        private int totalNumberOfMessagesToSend = Integer.MIN_VALUE;
        private int skippedResponses = Integer.MIN_VALUE;
        private int respondToEveryNthRequest = Integer.MIN_VALUE;
        private int sendingRatePerSecond = Integer.MIN_VALUE;

        private int optionsOffset = DEFAULT_OPTIONS_OFFSET;
        private int sendTimeOffset = DEFAULT_SEND_TIME_OFFSET;
        private int correlationIdOffset = DEFAULT_CORRELATION_ID_OFFSET;

        private Configuration(final String description)
        {
            this.description = description;
        }


        public int optionsOffset()
        {
            return optionsOffset;
        }

        public int sendTimeOffset()
        {
            return sendTimeOffset;
        }

        public int correlationIdOffset()
        {
            return correlationIdOffset;
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

        public Configuration metadataOffsets(final int optionsOffset, final int sendTimeOffset, final int correlationIdOffset)
        {
            this.optionsOffset = optionsOffset;
            this.sendTimeOffset = sendTimeOffset;
            this.correlationIdOffset = correlationIdOffset;
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
            return new Probe(
                    description,
                    new Metadata(optionsOffset, sendTimeOffset, correlationIdOffset),
                    totalNumberOfMessagesToSend, skippedResponses, respondToEveryNthRequest, sendingRatePerSecond
            );
        }
    }
}
