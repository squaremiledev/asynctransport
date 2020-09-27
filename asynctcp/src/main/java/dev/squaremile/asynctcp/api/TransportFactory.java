package dev.squaremile.asynctcp.api;

import java.io.IOException;


import dev.squaremile.asynctcp.serialization.api.MessageDrivenTransport;
import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.transport.api.values.PredefinedTransportEncoding;

public interface TransportFactory
{
    MessageDrivenTransport createMessageDrivenTransport(
            final String role,
            final PredefinedTransportEncoding predefinedTransportEncoding,
            final SerializedEventListener serializedEventListener
    ) throws IOException;
}
