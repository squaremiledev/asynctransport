package dev.squaremile.asynctcp.serialization;

import dev.squaremile.asynctcp.api.app.OnDuty;

public interface MessageDrivenTransport extends OnDuty, AutoCloseable, SerializedCommandListener
{
}
