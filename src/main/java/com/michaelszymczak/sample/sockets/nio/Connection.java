package com.michaelszymczak.sample.sockets.nio;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.michaelszymczak.sample.sockets.api.commands.CloseConnection;
import com.michaelszymczak.sample.sockets.api.commands.Command;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.commands.SendData;
import com.michaelszymczak.sample.sockets.connection.ConnectionAggregate;
import com.michaelszymczak.sample.sockets.connection.ConnectionEventsListener;

public class Connection implements AutoCloseable, ConnectionAggregate
{
    private final SocketChannel channel;
    private final int port;
    private final long connectionId;
    private final int remotePort;
    private final int initialSenderBufferSize;
    private final ThisConnectionEvents thisConnectionEvents;
    private long totalBytesSent;
    private long totalBytesReceived;
    private boolean isClosed = false;

    public Connection(final int port, final long connectionId, final int remotePort, final SocketChannel channel, final ConnectionEventsListener eventsListener) throws SocketException
    {
        this.port = port;
        this.connectionId = connectionId;
        this.remotePort = remotePort;
        this.channel = channel;
        this.initialSenderBufferSize = channel.socket().getSendBufferSize();
        this.thisConnectionEvents = new ThisConnectionEvents(eventsListener, port, connectionId);
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
        if (!validate(command))
        {
            return;
        }

        if (command instanceof CloseConnection)
        {
            handle((CloseConnection)command);
        }
        else if (command instanceof SendData)
        {
            handle((SendData)command);
        }
        else
        {
            thisConnectionEvents.commandFailed(command, "Unrecognized command");
        }
    }

    @Override
    public boolean isClosed()
    {
        return isClosed;
    }

    private void handle(final CloseConnection command)
    {
        closeConnection(command.commandId());
    }

    private void handle(final SendData command)
    {
        try
        {
            final int bytesSent = channel.write(ByteBuffer.wrap(command.content(), 0, Math.min(command.content().length, initialSenderBufferSize)));
            if (bytesSent > 0)
            {
                totalBytesSent += bytesSent;
            }
            // TODO: test a SendData command with no data
            thisConnectionEvents.dataSent(bytesSent, totalBytesSent, command.commandId());
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
            if (read == -1)
            {
                isClosed = true;
                thisConnectionEvents.connectionClosed(Command.NO_COMMAND_ID);
                return;
            }

            if (read > 0)
            {
                totalBytesReceived += read;
            }
            thisConnectionEvents.dataReceived(totalBytesReceived, content, read);
        }
        catch (IOException e)
        {
            if ("Connection reset by peer".equalsIgnoreCase(e.getMessage()))
            {
                isClosed = true;
                thisConnectionEvents.connectionResetByPeer(Command.NO_COMMAND_ID);
            }
        }
        catch (Exception e)
        {
            // TODO: return failure
            throw new RuntimeException(e);
        }
    }

    private boolean validate(final ConnectionCommand command)
    {
        if (command.connectionId() != connectionId)
        {
            thisConnectionEvents.commandFailed(command, "Incorrect connection id");
        }
        if (command.port() != port)
        {
            thisConnectionEvents.commandFailed(command, "Incorrect port");
            return false;
        }
        return true;
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
        closeConnection(Command.NO_COMMAND_ID);
    }

    private void closeConnection(final long conventionalIgnoredCommandId)
    {
        Resources.close(channel);
        if (!isClosed)
        {
            thisConnectionEvents.connectionClosed(conventionalIgnoredCommandId);
            isClosed = true;
        }
    }
}
