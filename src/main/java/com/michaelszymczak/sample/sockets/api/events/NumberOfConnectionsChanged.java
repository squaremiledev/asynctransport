package com.michaelszymczak.sample.sockets.api.events;

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
    public String toString()
    {
        return "NumberOfConnectionsChanged{" +
               "newNumberOfConnections=" + newNumberOfConnections +
               '}';
    }
}
