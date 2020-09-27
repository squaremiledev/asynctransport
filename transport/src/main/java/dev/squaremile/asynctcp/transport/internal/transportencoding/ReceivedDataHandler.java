package dev.squaremile.asynctcp.transport.internal.transportencoding;

import dev.squaremile.asynctcp.transport.api.events.DataReceived;

public interface ReceivedDataHandler
{
    void onDataReceived(final DataReceived event);
}
