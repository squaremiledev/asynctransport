package dev.squaremile.asynctcp.serialization;

import org.agrona.DirectBuffer;

public interface SerializedCommandListener
{
    void onSerializedCommand(DirectBuffer buffer, int offset);
}
