package com.michaelszymczak.sample.sockets.nio;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.michaelszymczak.sample.sockets.api.commands.CloseConnection;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.commands.SendData;
import com.michaelszymczak.sample.sockets.api.commands.TransportCommand;
import com.michaelszymczak.sample.sockets.api.events.ConnectionClosed;
import com.michaelszymczak.sample.sockets.api.events.ConnectionCommandFailed;
import com.michaelszymczak.sample.sockets.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.api.events.DataSent;
import com.michaelszymczak.sample.sockets.connection.ConnectionAggregate;
import com.michaelszymczak.sample.sockets.connection.ConnectionEventsListener;

public class Connection implements AutoCloseable, ConnectionAggregate
{
    private final SocketChannel channel;
    private final ConnectionEventsListener eventsListener;
    private final int port;
    private final long connectionId;
    private final int remotePort;
    private final int initialSenderBufferSize;
    private long totalBytesSent;
    private long totalBytesReceived;
    private boolean isClosed = false;

    public Connection(
            final int port,
            final long connectionId,
            final int remotePort,
            final SocketChannel channel,
            final ConnectionEventsListener eventsListener
    ) throws SocketException
    {
        this.port = port;
        this.connectionId = connectionId;
        this.remotePort = remotePort;
        this.channel = channel;
        this.initialSenderBufferSize = channel.socket().getSendBufferSize();
        this.eventsListener = eventsListener;
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
            eventsListener.onEvent(new ConnectionCommandFailed(command, "Incorrect port"));
            return;
        }

        if (command instanceof CloseConnection)
        {
            Resources.close(channel);
            if (!isClosed)
            {
                eventsListener.onEvent(new ConnectionClosed(port, connectionId, command.commandId()));
                isClosed = true;
            }
        }
        else if (command instanceof SendData)
        {
            sendData((SendData)command);
        }
        else
        {
            throw new IllegalArgumentException(command.getClass().getSimpleName());
        }
    }

    @Override
    public boolean isClosed()
    {
        return isClosed;
    }

    public int remotePort()
    {
        return remotePort;
    }

    public SocketChannel channel()
    {
        return channel;
    }

    public int initialSenderBufferSize()
    {
        return initialSenderBufferSize;
    }

    @Override
    public void close()
    {
        Resources.close(channel);
        if (!isClosed)
        {
            eventsListener.onEvent(new ConnectionClosed(port, connectionId, TransportCommand.CONVENTIONAL_IGNORED_COMMAND_ID));
            isClosed = true;
        }
    }

    private void sendData(final SendData command)
    {
        try
        {
            final int bytesSent = channel.write(ByteBuffer.wrap(command.content(), 0, Math.min(command.content().length, initialSenderBufferSize)));
            if (bytesSent > 0)
            {
                totalBytesSent += bytesSent;
            }
            // TODO: test a SendData command with no data
            eventsListener.onEvent(new DataSent(port, connectionId, bytesSent, totalBytesSent, command.commandId()));
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
            eventsListener.onEvent(new DataReceived(port, connectionId, totalBytesReceived, content, read));
        }
        catch (Exception e)
        {
            // TODO: return failure
            throw new RuntimeException(e);
        }
    }
}
