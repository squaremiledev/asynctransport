package dev.squaremile.asynctcp.serialization.api;

import dev.squaremile.asynctcp.transport.api.app.TransportOnDuty;

public interface MessageDrivenTransport extends TransportOnDuty, AutoCloseable, SerializedCommandListener
{
}
