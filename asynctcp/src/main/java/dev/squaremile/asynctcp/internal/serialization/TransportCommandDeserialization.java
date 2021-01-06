package dev.squaremile.asynctcp.internal.serialization;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.api.serialization.SerializedCommandListener;
import dev.squaremile.asynctcp.api.transport.app.Transport;

public class TransportCommandDeserialization implements SerializedCommandListener
{
    private final TransportCommandDecoders decoders;
    private final Transport transport;

    public TransportCommandDeserialization(final Transport transport)
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
