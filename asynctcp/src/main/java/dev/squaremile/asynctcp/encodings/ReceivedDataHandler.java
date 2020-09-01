package dev.squaremile.asynctcp.encodings;

import dev.squaremile.asynctcp.domain.api.events.DataReceived;

public interface ReceivedDataHandler
{
    void onDataReceived(final DataReceived event);
}
