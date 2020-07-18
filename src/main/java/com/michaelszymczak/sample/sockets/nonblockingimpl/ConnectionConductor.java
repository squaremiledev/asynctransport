package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.nio.channels.SelectionKey;

import com.michaelszymczak.sample.sockets.api.ConnectionId;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.commands.NoOpCommand;
import com.michaelszymczak.sample.sockets.api.commands.ReadData;

public class ConnectionConductor implements ConnectionId
{
    private final ReadData readDataCommand;
    private final NoOpCommand noOpCommand;

    public ConnectionConductor(final ReadData readDataCommand, final NoOpCommand noOpCommand)
    {
        this.readDataCommand = readDataCommand;
        this.noOpCommand = noOpCommand;
    }

    @Override
    public int port()
    {
        return noOpCommand.port();
    }

    @Override
    public long connectionId()
    {
        return noOpCommand.connectionId();
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
