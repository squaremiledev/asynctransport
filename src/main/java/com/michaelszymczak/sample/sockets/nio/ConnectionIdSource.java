package com.michaelszymczak.sample.sockets.nio;

public class ConnectionIdSource
{
    private long nextId = 0;

    public long newId()
    {
        return nextId++;
    }
}
