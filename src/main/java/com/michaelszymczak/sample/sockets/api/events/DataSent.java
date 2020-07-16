package com.michaelszymczak.sample.sockets.api.events;

import com.michaelszymczak.sample.sockets.api.commands.Command;

public class DataSent implements ConnectionEvent, TransportCorrelatedEvent
{
    private final int port;
    private final long connectionId;
    private final int bytesSent;
    private final long totalBytesSent;
    private final long commandId;

    public DataSent(final int port, final long connectionId, final int bytesSent, final long totalBytesSent)
    {
        this(port, connectionId, bytesSent, totalBytesSent, Command.NO_COMMAND_ID);
    }

    public DataSent(final int port, final long connectionId, final int bytesSent, final long totalBytesSent, final long commandId)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.bytesSent = bytesSent;
        this.totalBytesSent = totalBytesSent;
        this.commandId = commandId;
    }

    @Override
    public int port()
    {
        return port;
    }

    @Override
    public long connectionId()
    {
        return connectionId;
    }

    public long totalBytesSent()
    {
        return totalBytesSent;
    }

    public int bytesSent()
    {
        return bytesSent;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    @Override
    public String toString()
    {
        return "DataSent{" +
               "port=" + port +
               ", connectionId=" + connectionId +
               ", bytesSent=" + bytesSent +
               ", totalBytesSent=" + totalBytesSent +
               ", commandId=" + commandId +
               '}';
    }
}
