package com.michaelszymczak.sample.sockets.events;

public class ConnectionEstablished implements TransportEvent
{
    @Override
    public int port()
    {
        return 0;
    }

    @Override
    public long commandId()
    {
        return 0;
    }
}
