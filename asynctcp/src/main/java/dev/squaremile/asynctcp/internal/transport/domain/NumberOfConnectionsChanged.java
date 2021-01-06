package dev.squaremile.asynctcp.internal.transport.domain;

public class NumberOfConnectionsChanged implements StatusEvent
{
    private final int newNumberOfConnections;

    public NumberOfConnectionsChanged(final int newNumberOfConnections)
    {
        this.newNumberOfConnections = newNumberOfConnections;
    }

    public int newNumberOfConnections()
    {
        return newNumberOfConnections;
    }

    @Override
    public boolean occursInSteadyState()
    {
        return false;
    }

    @Override
    public String toString()
    {
        return "NumberOfConnectionsChanged{" +
               "newNumberOfConnections=" + newNumberOfConnections +
               '}';
    }

    @Override
    public StatusEvent copy()
    {
        return new NumberOfConnectionsChanged(newNumberOfConnections);
    }
}
