package dev.squaremile.asynctcp.internal.transportencoding;

import dev.squaremile.asynctcp.api.events.DataReceived;

public interface ReceivedDataHandler
{
    void onDataReceived(final DataReceived event);
}
