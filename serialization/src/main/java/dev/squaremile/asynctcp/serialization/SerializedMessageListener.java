package dev.squaremile.asynctcp.serialization;

import org.agrona.DirectBuffer;

public interface SerializedMessageListener
{
    void onSerialized(DirectBuffer sourceBuffer, int sourceOffset, final int length);
}
