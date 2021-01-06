package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.nio.channels.SelectionKey;


import dev.squaremile.asynctcp.api.transport.app.ConnectionCommand;
import dev.squaremile.asynctcp.api.transport.commands.SendData;
import dev.squaremile.asynctcp.api.transport.values.ConnectionId;
import dev.squaremile.asynctcp.internal.transport.domain.NoOpCommand;
import dev.squaremile.asynctcp.internal.transport.domain.ReadData;

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
