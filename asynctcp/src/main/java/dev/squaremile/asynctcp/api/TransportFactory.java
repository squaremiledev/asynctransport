package dev.squaremile.asynctcp.api;

import java.io.IOException;


import dev.squaremile.asynctcp.serialization.api.MessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

public interface TransportFactory
{
    MessageDrivenTransport createMessageDrivenTransport(
            final String role,
            final Delineation predefinedTransportDelineation,
            final SerializedEventListener serializedEventListener
    ) throws IOException;
}
