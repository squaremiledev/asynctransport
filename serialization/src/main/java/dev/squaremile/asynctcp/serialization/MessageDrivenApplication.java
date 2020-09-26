package dev.squaremile.asynctcp.serialization;

import dev.squaremile.asynctcp.api.app.Application;

public interface MessageDrivenApplication extends Application, SerializedEventListener
{
}
