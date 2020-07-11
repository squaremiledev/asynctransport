package com.michaelszymczak.sample.sockets.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.michaelszymczak.sample.sockets.api.TransportEventsListener;
import com.michaelszymczak.sample.sockets.api.commands.CloseConnection;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.commands.SendData;
import com.michaelszymczak.sample.sockets.api.events.CommandFailed;
import com.michaelszymczak.sample.sockets.connection.ConnectionAggregate;

public class Connection implements AutoCloseable, ConnectionAggregate
{
    private final SocketChannel channel;
    private final TransportEventsListener transportEventsListener;
    private final int port;
    private final long connectionId;
    private final int remotePort;

    public Connection(
            final int port,
            final long connectionId,
            final int remotePort,
            final SocketChannel channel,
            final TransportEventsListener transportEventsListener
    )
    {
        this.port = port;
        this.connectionId = connectionId;
        this.remotePort = remotePort;
        this.channel = channel;
        this.transportEventsListener = transportEventsListener;
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

    @Override
    public void handle(final ConnectionCommand command)
    {
        if (command.connectionId() != connectionId)
        {
            throw new IllegalArgumentException();
        }
        if (command.port() != port)
        {
            transportEventsListener.onEvent(new CommandFailed(command.port(), command.connectionId(), "Incorrect port"));
            return;
        }

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
