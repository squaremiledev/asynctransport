package dev.squaremile.asynctcp.serialization;

import org.agrona.DirectBuffer;

public interface SerializedCommandListener
{
    // consider something usable with
    // https://appdoc.app/artifact/io.aeron/aeron-all/1.10.2/io/aeron/Publication.html#offer-org.agrona.DirectBuffer-
    void onSerializedCommand(DirectBuffer buffer, int offset);
}
