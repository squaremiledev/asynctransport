package dev.squaremile.asynctcp.serialization.api;

import dev.squaremile.asynctcp.transport.api.app.OnDuty;

public interface MessageDrivenTransport extends OnDuty, AutoCloseable, SerializedCommandListener
{
}
