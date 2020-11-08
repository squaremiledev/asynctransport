package dev.squaremile.asynctcp.serialization.internal;

import org.agrona.DirectBuffer;

public interface SerializedMessageListener
{
    SerializedMessageListener NO_OP = (sourceBuffer, sourceOffset, length) ->
    {
    };

    void onSerialized(DirectBuffer sourceBuffer, int sourceOffset, final int length);
}
