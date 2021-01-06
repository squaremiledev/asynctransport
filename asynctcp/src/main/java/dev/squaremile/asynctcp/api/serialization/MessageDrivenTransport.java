package dev.squaremile.asynctcp.api.serialization;

import dev.squaremile.asynctcp.api.transport.app.TransportOnDuty;

public interface MessageDrivenTransport extends TransportOnDuty, AutoCloseable, SerializedCommandListener
{
}
