package dev.squaremile.asynctcp.serialization.internal;

import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;

public interface MessageDrivenApplication extends EventDrivenApplication, SerializedEventListener
{
}
