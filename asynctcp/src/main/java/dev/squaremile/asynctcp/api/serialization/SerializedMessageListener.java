package dev.squaremile.asynctcp.api.serialization;

import org.agrona.DirectBuffer;

public interface SerializedMessageListener
{
    SerializedMessageListener NO_OP = (sourceBuffer, sourceOffset, length) ->
    {
    };

    void onSerialized(DirectBuffer sourceBuffer, int sourceOffset, final int length);
}
