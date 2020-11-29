package dev.squaremile.aeroncluster.support.applications;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.agrona.BitUtil;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.IntArrayList;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;


import dev.squaremile.aeroncluster.api.ClusterClientApplication;
import dev.squaremile.aeroncluster.api.ClusterClientPublisher;
import io.aeron.cluster.client.AeronCluster;
import io.aeron.logbuffer.Header;

public class NumberGeneratorClusterClientApp implements ClusterClientApplication
{
    private final MutableDirectBuffer sendBuffer = new ExpandableDirectByteBuffer();
    private final IdleStrategy idleStrategy = new BackoffIdleStrategy();
    private final ClusterClientPublisher publisher;
    private final AeronCluster aeronCluster;
    private final int messageCount;
    private final IntArrayList messages;
    private final IntArrayList receivedMessages;
    private final int intervalMs;
    private final long expectedTotal;
    private long totalSoFar;

    public NumberGeneratorClusterClientApp(final AeronCluster aeronCluster, final ClusterClientPublisher publisher, final int messageCount, final int intervalMs)
    {
        this.aeronCluster = aeronCluster;
        this.publisher = publisher;
        this.messageCount = messageCount;
        this.messages = new IntArrayList(IntStream.generate(() -> ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE)).limit(messageCount).toArray(), messageCount, Integer.MIN_VALUE);
        this.expectedTotal = messages.stream().mapToLong(value -> value).sum();
        this.receivedMessages = new IntArrayList(messageCount, Integer.MIN_VALUE);
        this.intervalMs = intervalMs;
    }

    @Override
    public void onMessage(final long clusterSessionId, final long timestamp, final DirectBuffer buffer, final int offset, final int length, final Header header)
    {
        int message = buffer.getInt(offset);
        totalSoFar = buffer.getLong(offset + BitUtil.SIZE_OF_INT);
        System.out.println("Received " + message + ", total so far: " + totalSoFar);
        receivedMessages.add(message);
    }

    @Override
    public void onStart()
    {
        final long startTimeMs = System.currentTimeMillis();
        final long finalDeadline = startTimeMs + (intervalMs * messageCount) + 1_000;
        int messagesSent = 0;
        long nextMessateDeadlineMs = startTimeMs;

        while (!Thread.currentThread().isInterrupted())
        {
            final long currentTimeMs = System.currentTimeMillis();

            if (receivedMessages.size() >= messages.size() || currentTimeMs > finalDeadline)
            {
                if (!receivedMessages.equals(messages))
                {
                    throw new IllegalStateException("Did not receive expected messages. \nActual: " + receivedMessages + "\nExpected: " + messages);
                }
                if (totalSoFar != expectedTotal)
                {
                    throw new IllegalStateException("Did not calculate the sum correctly, Actual: " + totalSoFar + ", Expected: " + expectedTotal);
                }
                else
                {
                    System.out.println("Done");
                }
                break;
            }

            if (currentTimeMs >= nextMessateDeadlineMs && messagesSent < messages.size())
            {
                final int message = messages.getInt(messagesSent);
                sendBuffer.putInt(0, message);
                publisher.publish(sendBuffer, 0, BitUtil.SIZE_OF_INT);
                System.out.println("Sent " + message);
                messagesSent++;
                System.out.println("Sent so far " + messagesSent);
                nextMessateDeadlineMs = currentTimeMs + intervalMs;
            }
            idleStrategy.idle(aeronCluster.pollEgress());
        }
    }
}
