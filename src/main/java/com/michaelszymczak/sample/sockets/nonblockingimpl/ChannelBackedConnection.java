package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.michaelszymczak.sample.sockets.api.CommandId;
import com.michaelszymczak.sample.sockets.api.commands.CloseConnection;
import com.michaelszymczak.sample.sockets.api.commands.CommandFactory;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.commands.NoOpCommand;
import com.michaelszymczak.sample.sockets.api.commands.ReadData;
import com.michaelszymczak.sample.sockets.api.commands.SendData;
import com.michaelszymczak.sample.sockets.connection.Channel;
import com.michaelszymczak.sample.sockets.connection.Connection;
import com.michaelszymczak.sample.sockets.connection.ConnectionCommands;
import com.michaelszymczak.sample.sockets.connection.ConnectionConfiguration;
import com.michaelszymczak.sample.sockets.connection.ConnectionEventsListener;
import com.michaelszymczak.sample.sockets.support.Resources;

public class ChannelBackedConnection implements AutoCloseable, Connection
{
    private final Channel channel;
    private final ThisConnectionEvents thisConnectionEvents;
    private final ConnectionCommands connectionCommands;
    private final ConnectionConfiguration configuration;
    private final ByteBuffer readyToSendBuffer;
    private long totalBytesSent;
    private long totalBytesBuffered;
    private long totalBytesReceived;
    private boolean isClosed = false;
    private ConnectionSendingState sendingState;

    ChannelBackedConnection(final ConnectionConfiguration configuration, final Channel channel, final ConnectionEventsListener eventsListener)
    {
        this(configuration, channel, eventsListener, new CommandFactory());
    }

    ChannelBackedConnection(final ConnectionConfiguration configuration, final Channel channel, final ConnectionEventsListener eventsListener, final CommandFactory commandFactory)
    {
        this.configuration = configuration;
        this.channel = channel;
        this.thisConnectionEvents = new ThisConnectionEvents(eventsListener, configuration.connectionId.port(), configuration.connectionId.connectionId());
        this.connectionCommands = new ConnectionCommands(commandFactory, configuration.connectionId, configuration.sendBufferSize);
        // TODO: size appropriately
        this.readyToSendBuffer = ByteBuffer.allocate(configuration.sendBufferSize);
        this.sendingState = ConnectionSendingState.EMPTY;

    }

    @Override
    public int port()
    {
        return configuration.connectionId.port();
    }

    @Override
    public long connectionId()
    {
        return configuration.connectionId.connectionId();
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

    // TODO: handle -1 as closed connection
    private void handle(final SendData command)
    {
        try
        {
            final int bytesSentFromBufferNow;
            final int previouslyBufferedBytes;
            try
            {
                readyToSendBuffer.flip();
                previouslyBufferedBytes = readyToSendBuffer.remaining();
                if (previouslyBufferedBytes > 0)
                {
                    final int result = channel.write(readyToSendBuffer);
                    bytesSentFromBufferNow = result >= 0 ? result : 0;
                }
                else
                {
                    bytesSentFromBufferNow = 0;
                }
            }
            finally
            {
                readyToSendBuffer.compact();
            }


            final ByteBuffer src = command.byteBuffer();
            final int bytesSentFromCommandNow;
            if (previouslyBufferedBytes == 0 && src.remaining() > 0)
            {
                final int result = channel.write(src);
                bytesSentFromCommandNow = result >= 0 ? result : 0;
            }
            else
            {
                bytesSentFromCommandNow = 0;
            }

            final int remainingToSend = src.remaining();
            if (remainingToSend > 0)
            {
                sendingState = ConnectionSendingState.NOT_ALL_DATA_SENT;
                readyToSendBuffer.put(src);
            }
            else
            {
                sendingState = ConnectionSendingState.ALL_DATA_SENT;
            }


            totalBytesBuffered += remainingToSend + bytesSentFromCommandNow;
            totalBytesSent += bytesSentFromCommandNow + bytesSentFromBufferNow;

            thisConnectionEvents.dataSent(bytesSentFromCommandNow + bytesSentFromBufferNow, totalBytesSent, totalBytesBuffered, command.commandId());
        }
        catch (IOException e)
        {
            // TODO: return failure
            throw new RuntimeException(e);
        }
    }

    private void handle(final ReadData command)
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
        final String result = configuration.connectionId.validate(command);
        if (result != null)
        {
            thisConnectionEvents.commandFailed(command, result);
            return false;
        }
        return true;
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

    @Override
    public String toString()
    {
        return "ChannelBackedConnection{" +
               "channel=" + channel +
               ", thisConnectionEvents=" + thisConnectionEvents +
               ", connectionCommands=" + connectionCommands +
               ", configuration=" + configuration +
               ", totalBytesSent=" + totalBytesSent +
               ", totalBytesReceived=" + totalBytesReceived +
               ", isClosed=" + isClosed +
               '}';
    }
}
