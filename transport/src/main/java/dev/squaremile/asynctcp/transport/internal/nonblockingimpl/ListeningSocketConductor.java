package dev.squaremile.asynctcp.transport.internal.nonblockingimpl;

public class ListeningSocketConductor
{
    private final int port;

    public ListeningSocketConductor(final int port)
    {
        this.port = port;
    }

    public int port()
    {
        return port;
    }
}
