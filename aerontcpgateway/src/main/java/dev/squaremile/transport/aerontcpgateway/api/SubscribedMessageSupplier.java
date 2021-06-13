package dev.squaremile.transport.aerontcpgateway.api;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.internal.serialization.messaging.MessageHandler;
import dev.squaremile.asynctcp.internal.serialization.messaging.MessageSupplier;
import io.aeron.FragmentAssembler;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;

class SubscribedMessageSupplier implements MessageSupplier
{
    private static final int FRAGMENT_LIMIT = 32; // no particular reason why this value exactly, the impact hasn't been measured yet so any number will do

    private final Subscription subscription;
    private final Handler handler = new Handler();
    private final FragmentAssembler fragmentAssembler = new FragmentAssembler(handler); // I would be nice to write a failing test showing the need for this one

    public SubscribedMessageSupplier(final Subscription subscription)
    {
        this.subscription = subscription;
    }

    @Override
    public int poll(final MessageHandler messageHandler)
    {
        handler.handleBy(messageHandler);
        return subscription.poll(fragmentAssembler, FRAGMENT_LIMIT);
    }

    private static class Handler implements FragmentHandler
    {
        private MessageHandler handler;

        public void handleBy(final MessageHandler handler)
        {
            this.handler = handler;
        }

        @Override
        public void onFragment(final DirectBuffer buffer, final int offset, final int length, final Header header)
        {
            handler.onMessage(buffer, offset, length);
        }
    }
}
