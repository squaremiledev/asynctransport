package dev.squaremile.transport.aerontcpgateway.api;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.internal.serialization.messaging.MessageHandler;
import dev.squaremile.asynctcp.internal.serialization.messaging.MessageSupplier;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;

class SubscribedMessageSupplier implements MessageSupplier
{
    private final Subscription subscription;
    private final Fragment fragment = new Fragment();

    public SubscribedMessageSupplier(final Subscription subscription)
    {
        this.subscription = subscription;
    }

    @Override
    public int poll(final MessageHandler handler)
    {
        int poll = subscription.poll(fragment.reset(), 1);
        if (fragment.hasData())
        {
            handler.onMessage(fragment.buffer, fragment.offset, fragment.length);
        }
        return poll;
    }

    private static class Fragment implements FragmentHandler
    {
        DirectBuffer buffer;
        int offset;
        int length;

        @Override
        public void onFragment(final DirectBuffer buffer, final int offset, final int length, final Header header)
        {
            this.buffer = buffer;
            this.offset = offset;
            this.length = length;
        }

        public FragmentHandler reset()
        {
            this.buffer = null;
            this.offset = 0;
            this.length = 0;
            return this;
        }

        public boolean hasData()
        {
            return buffer != null && length > 0;
        }
    }
}
