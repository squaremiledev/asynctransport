package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.nio.channels.SelectionKey;

import com.michaelszymczak.sample.sockets.domain.api.ConnectionId;
import com.michaelszymczak.sample.sockets.domain.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.domain.api.commands.NoOpCommand;
import com.michaelszymczak.sample.sockets.domain.api.commands.ReadData;
import com.michaelszymczak.sample.sockets.domain.api.commands.SendData;

public class ConnectionConductor implements ConnectionId
{
    private final ReadData readDataCommand;
    private final SendData sendDataCommand;
    private final NoOpCommand noOpCommand;

    public ConnectionConductor(final ReadData readDataCommand, final SendData sendDataCommand, final NoOpCommand noOpCommand)
    {
        this.readDataCommand = readDataCommand;
        this.sendDataCommand = sendDataCommand;
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
        if (!key.isValid())
        {
            return noOpCommand;
        }
        if (key.isReadable())
        {
            return readDataCommand;
        }
        if (key.isWritable())
        {
            return sendDataCommand;
        }
        return noOpCommand;
    }
}
