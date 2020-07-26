package com.michaelszymczak.sample.sockets.nonblockingimpl;

public class ConnectedNotification
{
    final long connectionId;
    final int localPort;
    final long commandId;

    public ConnectedNotification(long connectionId, int localPort, long commandId)
    {
        this.connectionId = connectionId;
        this.localPort = localPort;
        this.commandId = commandId;
    }
}
