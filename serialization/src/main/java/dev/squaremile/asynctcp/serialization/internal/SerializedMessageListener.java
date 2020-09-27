package dev.squaremile.asynctcp.serialization.internal;

import org.agrona.DirectBuffer;

public interface SerializedMessageListener
{
    void onSerialized(DirectBuffer sourceBuffer, int sourceOffset, final int length);
}
