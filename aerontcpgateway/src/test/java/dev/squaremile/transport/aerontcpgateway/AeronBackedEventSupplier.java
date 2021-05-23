package dev.squaremile.transport.aerontcpgateway;

import dev.squaremile.asynctcp.internal.serialization.messaging.MessageHandler;
import dev.squaremile.asynctcp.internal.serialization.messaging.SerializedEventSupplier;
import io.aeron.Subscription;

class AeronBackedEventSupplier implements SerializedEventSupplier
{
    private final Subscription subscription;
    private final Fragment fragment = new Fragment();

    public AeronBackedEventSupplier(final Subscription subscription)
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
}
