package com.michaelszymczak.sample.sockets.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.michaelszymczak.sample.sockets.api.commands.CloseConnection;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.commands.SendData;

public class Connection implements AutoCloseable
{
    private final SocketChannel channel;
    private final int port;
    private final long connectionId;
    private final int remotePort;

    public Connection(final int port, final long connectionId, final int remotePort, final SocketChannel channel)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.remotePort = remotePort;
        this.channel = channel;
    }

    public int port()
    {
        return port;
    }

    public long connectionId()
    {
        return connectionId;
    }

    public int remotePort()
    {
        return remotePort;
    }

    public SocketChannel channel()
    {
        return channel;
    }

    public void write(final byte[] content) throws IOException
    {
        channel.write(ByteBuffer.wrap(content));
    }

    @Override
    public void close()
    {
        Resources.close(channel);
    }

    public void handle(final ConnectionCommand command)
    {
        // TODO: handle non existing port or connectionId
        if (command instanceof CloseConnection)
        {
            // no op here at the moment
        }
        else if (command instanceof SendData)
        {
            final SendData cmd = (SendData)command;
            sendData(cmd.port(), cmd.connectionId(), cmd.content());
        }
        else
        {
            throw new IllegalArgumentException(command.getClass().getSimpleName());
        }
    }

    private void sendData(final int port, final long connectionId, final byte[] content)
    {
        try
        {
            write(content);
        }
        catch (IOException e)
        {
            // TODO: return failure
            throw new RuntimeException(e);
        }
    }
}
