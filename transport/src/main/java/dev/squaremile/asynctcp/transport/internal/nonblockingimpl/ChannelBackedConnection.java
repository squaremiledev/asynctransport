package dev.squaremile.asynctcp.transport.internal.nonblockingimpl;

import java.io.IOException;

import org.agrona.CloseHelper;

import static org.agrona.LangUtil.rethrowUnchecked;


import dev.squaremile.asynctcp.transport.api.app.ConnectionCommand;
import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.values.CommandId;
import dev.squaremile.asynctcp.transport.internal.domain.NoOpCommand;
import dev.squaremile.asynctcp.transport.internal.domain.ReadData;
import dev.squaremile.asynctcp.transport.internal.domain.connection.Channel;
import dev.squaremile.asynctcp.transport.internal.domain.connection.Connection;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionCommands;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionConfiguration;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionState;
import dev.squaremile.asynctcp.transport.internal.domain.connection.SingleConnectionEvents;

import static dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionState.CLOSED;

public class ChannelBackedConnection implements AutoCloseable, Connection
{
    private final Channel channel;
    private final SingleConnectionEvents singleConnectionEvents;
    private final ConnectionCommands connectionCommands;
    private final ConnectionConfiguration configuration;
    private final OutgoingStream outgoingStream;
    private long totalBytesReceived;
    private ConnectionState connectionState;
    private int port;

    ChannelBackedConnection(
            final ConnectionConfiguration configuration,
            final Channel channel,
            final SingleConnectionEvents singleConnectionEvents
    )
    {
        this.configuration = configuration;
        this.channel = channel;
        this.singleConnectionEvents = singleConnectionEvents;
        this.connectionCommands = new ConnectionCommands(configuration.connectionId, configuration.outboundPduLimit);
        this.outgoingStream = new OutgoingStream(this.singleConnectionEvents, configuration.sendBufferSize);
        this.connectionState = outgoingStream.state();
        this.port = configuration.connectionId.port();
    }

    @Override
    public int port()
    {
        return port;
    }

    @Override
    public long connectionId()
    {
        return configuration.connectionId.connectionId();
    }

    @Override
    public boolean handle(final ConnectionCommand command)
    {
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
        else if (command instanceof SendMessage)
        {
            handle((SendMessage)command);
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
    public <C extends ConnectionUserCommand> C command(final Class<C> commandType)
    {
        return connectionCommands.command(commandType);
    }

    @Override
    public ConnectionState state()
    {
        return connectionState;
    }

    @Override
    public void accepted(final long commandIdThatTriggeredListening)
    {
        singleConnectionEvents.onEvent(new ConnectionAccepted(
                this.port,
                commandIdThatTriggeredListening,
                configuration.remoteHost,
                configuration.remotePort,
                configuration.connectionId.connectionId(),
                configuration.inboundPduLimit,
                configuration.outboundPduLimit
        ));
    }

    @Override
    public void connected(final long commandId)
    {
        singleConnectionEvents.onEvent(new Connected(
                this.port,
                commandId,
                configuration.remoteHost,
                configuration.remotePort,
                configuration.connectionId.connectionId(),
                configuration.inboundPduLimit,
                configuration.outboundPduLimit
        ));
    }

    private void handle(final CloseConnection command)
    {
        closeConnection(command.commandId(), false);
    }

    private void handle(final SendData command)
    {
        try
        {
            connectionState = outgoingStream.sendData(channel, command.data(), command.commandId());
        }
        catch (IOException e)
        {
            rethrowUnchecked(e);
        }
    }

    private void handle(final SendMessage command)
    {
        try
        {
            connectionState = outgoingStream.sendData(channel, command.data(), command.commandId());
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
            DataReceived dataReceivedEvent = singleConnectionEvents.dataReceivedEvent();
            final int readLength = channel.read(dataReceivedEvent.prepareForWriting());
            if (readLength == -1)
            {
                closeConnection(CommandId.NO_COMMAND_ID, false);
                return;
            }

            if (readLength > 0)
            {
                totalBytesReceived += readLength;
            }

            singleConnectionEvents.onEvent(dataReceivedEvent.commitWriting(readLength, totalBytesReceived));
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

    @Override
    public void close()
    {
        closeConnection(CommandId.NO_COMMAND_ID, false);
    }

    private void closeConnection(final long commandId, final boolean resetByPeer)
    {
        CloseHelper.close(channel);
        if (connectionState != CLOSED)
        {
            if (resetByPeer)
            {
                singleConnectionEvents.connectionResetByPeer(commandId);
            }
            else
            {
                singleConnectionEvents.connectionClosed(commandId);
            }
            connectionState = CLOSED;
        }
    }

    @Override
    public String toString()
    {
        return "ChannelBackedConnection{" +
               "configuration=" + configuration +
               ", channel=" + channel +
               ", singleConnectionEvents=" + singleConnectionEvents +
               ", connectionCommands=" + connectionCommands +
               ", outgoingStream=" + outgoingStream +
               ", totalBytesReceived=" + totalBytesReceived +
               '}';
    }
}
