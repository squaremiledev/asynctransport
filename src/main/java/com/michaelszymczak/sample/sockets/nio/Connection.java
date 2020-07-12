package com.michaelszymczak.sample.sockets.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.michaelszymczak.sample.sockets.api.TransportEventsListener;
import com.michaelszymczak.sample.sockets.api.commands.CloseConnection;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.commands.SendData;
import com.michaelszymczak.sample.sockets.api.events.CommandFailed;
import com.michaelszymczak.sample.sockets.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.api.events.DataSent;
import com.michaelszymczak.sample.sockets.connection.ConnectionAggregate;

public class Connection implements AutoCloseable, ConnectionAggregate
{
    private final SocketChannel channel;
    private final TransportEventsListener transportEventsListener;
    private final int port;
    private final long connectionId;
    private final int remotePort;
    private long totalBytesSent;
    private long totalBytesReceived;

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
            sendData(cmd.content());
        }
        else
        {
            throw new IllegalArgumentException(command.getClass().getSimpleName());
        }
    }

    public int remotePort()
    {
        return remotePort;
    }

    public SocketChannel channel()
    {
        return channel;
    }

    @Override
    public void close()
    {
        Resources.close(channel);
    }

    private void sendData(final byte[] content)
    {
        try
        {
            final int bytesSent = channel.write(ByteBuffer.wrap(content));
            if (bytesSent > 0)
            {
                totalBytesSent += bytesSent;
            }
            transportEventsListener.onEvent(new DataSent(port, connectionId, totalBytesSent));
        }
        catch (IOException e)
        {
            // TODO: return failure
            throw new RuntimeException(e);
        }
    }

    public void read()
    {
        final byte[] content = new byte[10];
        final ByteBuffer readBuffer = ByteBuffer.wrap(content);
        try
        {
            final int read = channel.read(readBuffer);
            if (read > 0)
            {
                totalBytesReceived += read;
            }
            transportEventsListener.onEvent(new DataReceived(port, connectionId, totalBytesReceived, content, read));
        }
        catch (Exception e)
        {
            // TODO: return failure
            throw new RuntimeException(e);
        }
    }
}
