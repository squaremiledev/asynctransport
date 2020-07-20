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
import com.michaelszymczak.sample.sockets.connection.ConnectionState;

import org.agrona.CloseHelper;

import static org.agrona.LangUtil.rethrowUnchecked;


import static com.michaelszymczak.sample.sockets.connection.ConnectionState.CLOSED;

public class ChannelBackedConnection implements AutoCloseable, Connection
{
    private final Channel channel;
    private final SingleConnectionEvents singleConnectionEvents;
    private final ConnectionCommands connectionCommands;
    private final ConnectionConfiguration configuration;
    private final OutgoingStream outgoingStream;
    private long totalBytesReceived;
    private boolean isClosed = false;
    private ConnectionState connectionState;
    private final ByteBuffer readBuffer;

    ChannelBackedConnection(final ConnectionConfiguration configuration, final Channel channel, final ConnectionEventsListener eventsListener)
    {
        this(configuration, channel, eventsListener, new CommandFactory());
    }

    ChannelBackedConnection(final ConnectionConfiguration configuration, final Channel channel, final ConnectionEventsListener eventsListener, final CommandFactory commandFactory)
    {
        this.configuration = configuration;
        this.channel = channel;
        this.singleConnectionEvents = new SingleConnectionEvents(eventsListener, configuration.connectionId.port(), configuration.connectionId.connectionId());
        this.connectionCommands = new ConnectionCommands(commandFactory, configuration.connectionId, configuration.maxOutboundMessageSize);
        this.outgoingStream = new OutgoingStream(singleConnectionEvents, configuration.sendBufferSize);
        this.connectionState = outgoingStream.state();
        this.readBuffer = ByteBuffer.wrap(new byte[configuration.maxInboundMessageSize]);
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
    public boolean handle(final ConnectionCommand command)
    {
        if (!validate(command))
        {
            return false;
        }

        if (command instanceof NoOpCommand)
        {
            return true;
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
            singleConnectionEvents.commandFailed(command, "Unrecognized command");
            return false;
        }
        return true;
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

    @Override
    public ConnectionState state()
    {
        return isClosed ? CLOSED : connectionState;
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
            connectionState = outgoingStream.sendData(channel, command.byteBuffer(), command.commandId());
        }
        catch (IOException e)
        {
            rethrowUnchecked(e);
        }
    }

    private void handle(final ReadData command)
    {
        try
        {
            // TODO: all tests passed without clearing the read buffer - this logic needs testing
            readBuffer.clear();
            final int readLength = channel.read(readBuffer);
            if (readLength == -1)
            {
                closeConnection(CommandId.NO_COMMAND_ID, false);
                return;
            }

            if (readLength > 0)
            {
                totalBytesReceived += readLength;
            }
            singleConnectionEvents.dataReceived(totalBytesReceived, readBuffer.array(), readLength);
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
            rethrowUnchecked(e);
        }
    }

    private boolean validate(final ConnectionCommand command)
    {
        final String result = configuration.connectionId.validate(command);
        if (result != null)
        {
            singleConnectionEvents.commandFailed(command, result);
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
        CloseHelper.close(channel);
        if (!isClosed)
        {
            if (resetByPeer)
            {
                singleConnectionEvents.connectionResetByPeer(commandId);
            }
            else
            {
                singleConnectionEvents.connectionClosed(commandId);
            }
            isClosed = true;
            connectionState = CLOSED;
        }
    }

    @Override
    public String toString()
    {
        return "ChannelBackedConnection{" +
               "channel=" + channel +
               ", singleConnectionEvents=" + singleConnectionEvents +
               ", connectionCommands=" + connectionCommands +
               ", configuration=" + configuration +
               ", outgoingStream=" + outgoingStream +
               ", totalBytesReceived=" + totalBytesReceived +
               ", isClosed=" + isClosed +
               '}';
    }
}
