package dev.squaremile.asynctcp.serialization.internal.messaging;

import org.agrona.concurrent.MessageHandler;

public interface MessageSupplier
{
    /**
     * Read as many messages as are available to the end of the buffer.
     *
     * @param handler to be called for processing each message in turn.
     */
    void poll(MessageHandler handler);
}
