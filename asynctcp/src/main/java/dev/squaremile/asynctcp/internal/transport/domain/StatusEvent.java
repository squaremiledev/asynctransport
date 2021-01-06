package dev.squaremile.asynctcp.internal.transport.domain;

import dev.squaremile.asynctcp.api.transport.app.Event;

public interface StatusEvent extends Event
{
    StatusEvent copy();
}
