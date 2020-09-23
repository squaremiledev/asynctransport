package dev.squaremile.asynctcp.internal.domain;

import dev.squaremile.asynctcp.api.app.Event;

public interface StatusEvent extends Event
{
    StatusEvent copy();
}
