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

    public Probe(final String description, final int totalNumberOfMessagesToSend, final int skippedResponses, final int respondToEveryNthRequest, final int sendingRatePerSecond)
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

    public int onTime(final long nowNs, final MutableDirectBuffer inboundBuffer, final int outboundOffset)
    {
        final long sendTimestampNs = calculateNextMessageSendingTimeNs(nowNs);
        if (sentAllMessages() || nowNs < sendTimestampNs)
        {
            return 0;
        }
        metadata.wrap(inboundBuffer, outboundOffset).clear().options().respond(expectsResponseForTheNextSendingMessage());
        metadata.originalTimestampNs(sendTimestampNs).correlationId(messagesSentCount());
        return metadata.length();
    }

    public SelectiveResponseRequest selectiveResponseRequest()
    {
        return selectiveResponseRequest;
    }

    public Measurements measurements()
    {
        return measurements;
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

    public boolean sentAllMessages()
    {
        return messagesSentCount >= messagesToSend;
    }

    public long messagesSentCount()
    {
        return messagesSentCount;
    }

    public void onMessageSent()
    {
        awaitingResponsesInFlightCount += expectsResponseForTheNextSendingMessage() ? 1 : 0;
        messagesSentCount++;
    }

    public boolean receivedAll()
    {
        boolean receivedAll = selectiveResponseRequest.receivedLast(messagesReceivedCount);
        if (receivedAll && awaitingResponsesInFlightCount != 0)
        {
            throw new IllegalStateException("At this point we should have received all expected responses, " +
                                            "but " + awaitingResponsesInFlightCount + " are still in flight");
        }
        return receivedAll;
    }

    public boolean expectsResponseForTheNextSendingMessage()
    {
        return selectiveResponseRequest.shouldRespond(messagesSentCount);
    }

    public long calculateNextMessageSendingTimeNs(final long nowNs)
    {
        final long result;
        if (startedSendingTimestampNanos != Long.MIN_VALUE)
        {
            result = startedSendingTimestampNanos + messagesSentCount * messageDelayNs;
        }
        else
        {
            result = nowNs;
            startedSendingTimestampNanos = nowNs;
        }
        return result;
    }
}
