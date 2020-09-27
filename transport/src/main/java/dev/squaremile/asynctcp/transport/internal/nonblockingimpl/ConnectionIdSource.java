package dev.squaremile.asynctcp.transport.internal.nonblockingimpl;

public class ConnectionIdSource
{
    private long nextId = 0;

    public long newId()
    {
        return nextId++;
    }
}
