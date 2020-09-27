package dev.squaremile.asynctcp.serialization;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.api.app.TransportCommandHandler;

public class TransportCommandDeserialization implements SerializedCommandListener
{
    private final TransportCommandDecoders decoders = new TransportCommandDecoders();
    private final TransportCommandHandler transportCommandHandler;

    public TransportCommandDeserialization(final TransportCommandHandler transportCommandHandler)
    {
        this.transportCommandHandler = transportCommandHandler;
    }

    @Override
    public void onSerializedCommand(final DirectBuffer buffer, final int offset)
    {
        transportCommandHandler.handle(decoders.decode(buffer, offset));
    }
}
