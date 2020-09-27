package dev.squaremile.asynctcp.transport.internal.domain;

import dev.squaremile.asynctcp.transport.api.app.Event;

public interface StatusEvent extends Event
{
    StatusEvent copy();
}
