package dev.squaremile.transport.aeron;

import dev.squaremile.asynctcp.internal.serialization.messaging.MessageHandler;
import dev.squaremile.asynctcp.internal.serialization.messaging.SerializedCommandSupplier;
import io.aeron.Subscription;

class AeronBackedCommandSupplier implements SerializedCommandSupplier
{
    private final Subscription userToNetworkSubscription;
    private final Fragment fragment = new Fragment();

    public AeronBackedCommandSupplier(final Subscription userToNetworkSubscription)
    {
        this.userToNetworkSubscription = userToNetworkSubscription;
    }

    @Override
    public int poll(final MessageHandler handler)
    {
        int poll = userToNetworkSubscription.poll(fragment.reset(), 1);
        if (fragment.hasData())
        {
            handler.onMessage(fragment.buffer, fragment.offset, fragment.length);
        }
        return poll;
    }
}
