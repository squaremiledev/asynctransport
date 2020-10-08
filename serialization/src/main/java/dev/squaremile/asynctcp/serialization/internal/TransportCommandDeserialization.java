package dev.squaremile.asynctcp.serialization.internal;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.serialization.api.SerializedCommandListener;
import dev.squaremile.asynctcp.transport.api.app.TransportOnDuty;

public class TransportCommandDeserialization implements SerializedCommandListener
{
    private final TransportCommandDecoders decoders;
    private final TransportOnDuty transport;

    public TransportCommandDeserialization(final TransportOnDuty transport)
    {
        this.transport = transport;
        this.decoders = new TransportCommandDecoders(transport);
    }

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        transport.handle(decoders.decode(sourceBuffer, sourceOffset, length));
    }
}
