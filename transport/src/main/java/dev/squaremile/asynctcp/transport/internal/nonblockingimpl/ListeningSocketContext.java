package dev.squaremile.asynctcp.transport.internal.nonblockingimpl;

import dev.squaremile.asynctcp.transport.api.values.Delineation;

public class ListeningSocketContext
{
    private final int port;
    private final Delineation delineation;

    public ListeningSocketContext(final int port, final Delineation delineation)
    {
        this.port = port;
        this.delineation = delineation;
    }

    public int port()
    {
        return port;
    }

    public Delineation delineation()
    {
        return delineation;
    }
}
