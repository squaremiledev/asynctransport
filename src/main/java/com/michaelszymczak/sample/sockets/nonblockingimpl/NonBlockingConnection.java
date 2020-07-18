package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.michaelszymczak.sample.sockets.api.CommandId;
import com.michaelszymczak.sample.sockets.api.ConnectionIdValue;
import com.michaelszymczak.sample.sockets.api.commands.CloseConnection;
import com.michaelszymczak.sample.sockets.api.commands.CommandFactory;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.commands.NoOpCommand;
import com.michaelszymczak.sample.sockets.api.commands.ReadData;
import com.michaelszymczak.sample.sockets.api.commands.SendData;
import com.michaelszymczak.sample.sockets.connection.Connection;
import com.michaelszymczak.sample.sockets.connection.ConnectionCommands;
import com.michaelszymczak.sample.sockets.connection.ConnectionEventsListener;
import com.michaelszymczak.sample.sockets.support.Resources;

public class NonBlockingConnection implements AutoCloseable, Connection
{
    private final ConnectionIdValue connectionId;
    private final SocketChannel channel;
    private final int remotePort;
    private final int initialSenderBufferSize;
    private final ThisConnectionEvents thisConnectionEvents;
    private final ConnectionCommands connectionCommands;
    private long totalBytesSent;
    private long totalBytesReceived;
    private boolean isClosed = false;

    public NonBlockingConnection(
            final CommandFactory commandFactory,
            final ConnectionEventsListener eventsListener,
            final SocketChannel channel,
            final ConnectionIdValue connectionId,
            final int remotePort
    ) throws SocketException
    {
        this.connectionId = connectionId;
        this.remotePort = remotePort;
        this.channel = channel;
        this.initialSenderBufferSize = channel.socket().getSendBufferSize();
        this.thisConnectionEvents = new ThisConnectionEvents(eventsListener, connectionId.port(), connectionId.connectionId());
        this.connectionCommands = new ConnectionCommands(commandFactory, connectionId, initialSenderBufferSize);
    }

    @Override
    public int port()
    {
        return connectionId.port();
    }

    @Override
    public long connectionId()
    {
        return connectionId.connectionId();
    }

    @Override
    public void handle(final ConnectionCommand command)
    {
        if (command instanceof NoOpCommand)
        {
            return;
        }
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
        else if (command instanceof ReadData)
        {
            handle((ReadData)command);
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

    @Override
    public <C extends ConnectionCommand> C command(final Class<C> commandType)
    {
        return connectionCommands.command(commandType);
    }

    private void handle(final CloseConnection command)
    {
        closeConnection(command.commandId(), false);
    }

    private void handle(final SendData command)
    {
        try
        {
            final int bytesSent = channel.write(command.byteBuffer());
            if (bytesSent > 0)
            {
                totalBytesSent += bytesSent;
            }
            thisConnectionEvents.dataSent(bytesSent, totalBytesSent, command.commandId());
        }
        catch (IOException e)
        {
            // TODO: return failure
            throw new RuntimeException(e);
        }
    }

    private void handle(final ReadData command)
    {
        read();
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
                closeConnection(CommandId.NO_COMMAND_ID, false);
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
                closeConnection(CommandId.NO_COMMAND_ID, true);
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
        final String result = connectionId.validate(command);
        if (result != null)
        {
            thisConnectionEvents.commandFailed(command, result);
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
        closeConnection(CommandId.NO_COMMAND_ID, false);
    }

    private void closeConnection(final long commandId, final boolean resetByPeer)
    {
        Resources.close(channel);
        if (!isClosed)
        {
            if (resetByPeer)
            {
                thisConnectionEvents.connectionResetByPeer(commandId);
            }
            else
            {
                thisConnectionEvents.connectionClosed(commandId);
            }
            isClosed = true;
        }
    }
}
