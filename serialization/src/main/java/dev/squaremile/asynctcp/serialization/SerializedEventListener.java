package dev.squaremile.asynctcp.serialization;

import org.agrona.DirectBuffer;

interface SerializedEventListener
{
    void onSerializedEvent(DirectBuffer buffer, int offset);
}
