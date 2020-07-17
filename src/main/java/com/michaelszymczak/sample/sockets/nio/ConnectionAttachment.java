package com.michaelszymczak.sample.sockets.nio;

import com.michaelszymczak.sample.sockets.api.ConnectionId;
import com.michaelszymczak.sample.sockets.api.commands.ReadData;

public class ConnectionAttachment implements ConnectionId
{
    private final ReadData readDataCommand;
    private final int port;
    private final long connectionId;

    public ConnectionAttachment(final int port, final long connectionId)
    {
        this.port = port;
        this.connectionId = connectionId;
        readDataCommand = new ReadData(port, connectionId);
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

    public ReadData readDataCommand()
    {
        return readDataCommand;
    }
}
