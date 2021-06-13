package dev.squaremile.transport.aerontcpgateway.api;

import java.util.concurrent.TimeUnit;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.YieldingIdleStrategy;


import dev.squaremile.asynctcp.api.serialization.SerializedMessageListener;
import io.aeron.ExclusivePublication;
import io.aeron.ReservedValueSupplier;

class SerializedMessagePublisher implements SerializedMessageListener
{
    public static final int FAILED_OFFER_TIMEOUT_MS = 10_000;

    private final ExclusivePublication publication;
    private final YieldingIdleStrategy idleStrategy = new YieldingIdleStrategy();
    private final String role;
    private final ReservedValueSupplier systemNanoTimeSupplier = (termBuffer, termOffset, frameLength) -> System.nanoTime();

    private long totalNumberOfSuccessfulOffers = 0;

    public SerializedMessagePublisher(final String role, final ExclusivePublication publication)
    {
        this.publication = publication;
        this.role = role;
    }

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        long offerResult = Long.MIN_VALUE;
        long firstFailedAttemptNs = Long.MIN_VALUE;
        do
        {
            if (publication.isConnected())
            {
                offerResult = publication.offer(sourceBuffer, sourceOffset, length, systemNanoTimeSupplier);
            }
            if (offerResult < 0)
            {
                if (firstFailedAttemptNs == Long.MIN_VALUE)
                {
                    firstFailedAttemptNs = System.nanoTime();
                }
                else
                {
                    final long timeSinceFirstFailedAttempt = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - firstFailedAttemptNs);
                    if (timeSinceFirstFailedAttempt > FAILED_OFFER_TIMEOUT_MS)
                    {
                        throw new IllegalStateException(
                                "Publisher '" + role + "' offer timeout" +
                                ", last result=" + offerResult +
                                ", timeout=" + timeSinceFirstFailedAttempt +
                                ", totalNumberOfSuccessfulOffers=" + totalNumberOfSuccessfulOffers
                        );
                    }
                }
                idleStrategy.idle();
            }
        }
        while (offerResult < 0);
        totalNumberOfSuccessfulOffers++;
    }
}
