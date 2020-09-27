package dev.squaremile.asynctcp.serialization;

import org.agrona.DirectBuffer;

public interface SerializedEventListener
{
    void onSerializedEvent(DirectBuffer buffer, int offset, final int length);
}
