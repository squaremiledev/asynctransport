package com.michaelszymczak.sample.sockets;

public class SessionIdSource
{
    private long nextId = 0;

    public long newId()
    {
        return nextId++;
    }
}
