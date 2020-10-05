package dev.squaremile.asynctcp.transport.internal.nonblockingimpl;

import dev.squaremile.asynctcp.transport.api.values.DelineationType;

public class ListeningSocketContext
{
    private final int port;
    private final DelineationType delineation;

    public ListeningSocketContext(final int port, final DelineationType delineation)
    {
        this.port = port;
        this.delineation = delineation;
    }

    public int port()
    {
        return port;
    }

    public DelineationType delineation()
    {
        return delineation;
    }
}
