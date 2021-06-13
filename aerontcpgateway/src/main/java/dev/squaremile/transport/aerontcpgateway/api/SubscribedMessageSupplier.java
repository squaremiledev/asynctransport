package dev.squaremile.transport.aerontcpgateway.api;

import java.util.concurrent.TimeUnit;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.internal.serialization.messaging.MessageHandler;
import dev.squaremile.asynctcp.internal.serialization.messaging.MessageSupplier;
import io.aeron.FragmentAssembler;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;

class SubscribedMessageSupplier implements MessageSupplier
{
    private static final boolean PRINT_DELAYS = false;
    private static final long PUB_SUB_DELAY_WARNING_THRESHOLD_NS = TimeUnit.MICROSECONDS.toNanos(200);
    private static final long WARNING_WARMUP_GRACE_PERIOD_NS = TimeUnit.SECONDS.toNanos(5);
    private static final int FRAGMENT_LIMIT = 32; // no particular reason why this value exactly, the impact hasn't been measured yet so any number will do

    private final Subscription subscription;
    private final Handler handler;
    private final FragmentAssembler fragmentAssembler; // I would be nice to write a failing test showing the need for this one

    public SubscribedMessageSupplier(final String role, final Subscription subscription)
    {
        this.subscription = subscription;
        this.handler = new Handler(role);
        this.fragmentAssembler = new FragmentAssembler(handler);
    }

    @Override
    public int poll(final MessageHandler messageHandler)
    {
        handler.handleBy(messageHandler);
        return subscription.poll(fragmentAssembler, FRAGMENT_LIMIT);
    }

    private static class Handler implements FragmentHandler
    {
        private final String role;

        private MessageHandler handler;
        private boolean firstFragmentHandled = false;
        private long firstFragmentHandledNs;
        private long lastTimeMessagePollRequested;
        private long lastTimeFragmentPollRequested;

        public Handler(final String role)
        {
            this.role = role;
        }

        public void handleBy(final MessageHandler handler)
        {
            this.lastTimeMessagePollRequested = System.nanoTime();
            this.lastTimeFragmentPollRequested = lastTimeMessagePollRequested;
            this.handler = handler;
        }

        @Override
        public void onFragment(final DirectBuffer buffer, final int offset, final int length, final Header header)
        {
            trackDelays(header.reservedValue());
            handler.onMessage(buffer, offset, length);
        }

        private void trackDelays(final long offeredSystemTimeNs)
        {
            if (!firstFragmentHandled)
            {
                firstFragmentHandledNs = System.nanoTime();
                firstFragmentHandled = true;
            }
            final long systemTimeNs = System.nanoTime();
            final long subscribedMessageReceivedDelayNs = systemTimeNs - offeredSystemTimeNs;
            final long timeSinceFirstFragmentHandledNs = systemTimeNs - firstFragmentHandledNs;
            final long timeSinceLastTimeMessagePollRequestedNs = systemTimeNs - lastTimeMessagePollRequested;
            final long timeSinceLastTimeFragmentPollRequestedNs = systemTimeNs - lastTimeFragmentPollRequested;
            this.lastTimeFragmentPollRequested = systemTimeNs;
            if (PRINT_DELAYS &&
                timeSinceFirstFragmentHandledNs > WARNING_WARMUP_GRACE_PERIOD_NS &&
                subscribedMessageReceivedDelayNs > PUB_SUB_DELAY_WARNING_THRESHOLD_NS)
            {
                System.out.println(
                        role +
                        ", pub - sub delay [us] " + TimeUnit.NANOSECONDS.toMicros(subscribedMessageReceivedDelayNs) +
                        ", last message poll delay [us] " + TimeUnit.NANOSECONDS.toMicros(timeSinceLastTimeMessagePollRequestedNs) +
                        ", last fragment poll delay [us] " + TimeUnit.NANOSECONDS.toMicros(timeSinceLastTimeFragmentPollRequestedNs) +
                        ", after first message handled [s] " + TimeUnit.NANOSECONDS.toSeconds(timeSinceFirstFragmentHandledNs)
                );
            }
        }
    }
}
