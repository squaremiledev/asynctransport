package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.nio.channels.SelectionKey;

import com.michaelszymczak.sample.sockets.api.ConnectionId;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.commands.NoOpCommand;
import com.michaelszymczak.sample.sockets.api.commands.ReadData;

public class ConnectionConductor implements ConnectionId
{
    private final int port;
    private final long connectionId;
    private final ReadData readDataCommand;
    private final NoOpCommand noOpCommand;

    public ConnectionConductor(final int port, final long connectionId)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.readDataCommand = new ReadData(port, connectionId);
        this.noOpCommand = new NoOpCommand(port, connectionId);
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

    public ConnectionCommand command(final SelectionKey key)
    {
        if (key.isValid() && key.isReadable())
        {
            return readDataCommand;
        }
        return noOpCommand;
    }
}
