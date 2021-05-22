package dev.squaremile.asynctcp.internal.serialization.messaging;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * Callback interface for processing of messages that are read from a buffer.
 */
public interface MessageHandler extends org.agrona.concurrent.MessageHandler
{
    /**
     * Called for the processing of each message read from a buffer in turn.
     *
     * @param buffer containing the encoded message.
     * @param offset at which the encoded message begins.
     * @param length in bytes of the encoded message.
     */
    void onMessage(DirectBuffer buffer, int offset, int length);

    @Override
    default void onMessage(int msgTypeId, MutableDirectBuffer buffer, int index, int length)
    {
        onMessage(buffer, index, length);
    }
}