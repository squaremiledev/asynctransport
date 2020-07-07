package com.michaelszymczak.sample.sockets;

public class RequestIdSource
{
    private long nextId = 0;

    public long newId()
    {
        return nextId++;
    }
}
