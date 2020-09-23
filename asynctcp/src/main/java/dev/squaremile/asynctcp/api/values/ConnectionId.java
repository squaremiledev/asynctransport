package dev.squaremile.asynctcp.api.values;

public interface ConnectionId extends TransportId
{
    int NO_CONNECTION = -1;

    long connectionId();
}
