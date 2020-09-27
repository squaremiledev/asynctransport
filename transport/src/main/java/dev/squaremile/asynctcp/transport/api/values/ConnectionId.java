package dev.squaremile.asynctcp.transport.api.values;

public interface ConnectionId extends TransportId
{
    int NO_CONNECTION = -1;

    long connectionId();
}
