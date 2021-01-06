package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import dev.squaremile.asynctcp.api.transport.values.Delineation;

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
