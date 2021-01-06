package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.io.IOException;

import org.agrona.CloseHelper;

import static org.agrona.LangUtil.rethrowUnchecked;


import dev.squaremile.asynctcp.api.transport.app.ConnectionCommand;
import dev.squaremile.asynctcp.api.transport.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.api.transport.app.OnDuty;
import dev.squaremile.asynctcp.api.transport.commands.CloseConnection;
import dev.squaremile.asynctcp.api.transport.commands.SendData;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.api.transport.events.Connected;
import dev.squaremile.asynctcp.api.transport.events.ConnectionAccepted;
import dev.squaremile.asynctcp.api.transport.events.DataReceived;
import dev.squaremile.asynctcp.api.transport.values.CommandId;
import dev.squaremile.asynctcp.api.transport.values.Delineation;
import dev.squaremile.asynctcp.internal.transport.domain.NoOpCommand;
import dev.squaremile.asynctcp.internal.transport.domain.ReadData;
import dev.squaremile.asynctcp.internal.transport.domain.connection.Channel;
import dev.squaremile.asynctcp.internal.transport.domain.connection.Connection;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionCommands;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionConfiguration;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionState;
import dev.squaremile.asynctcp.internal.transport.domain.connection.SingleConnectionEvents;

import static dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionState.CLOSED;

public class ChannelBackedConnection implements AutoCloseable, Connection, OnDuty
{
    private final Channel channel;
    private final Delineation delineation;
    private final SingleConnectionEvents singleConnectionEvents;
    private final ConnectionCommands connectionCommands;
    private final ConnectionConfiguration configuration;
    private final BufferedOutgoingStream outgoingStream;
    private final int port;
    private long totalBytesReceived;
    private ConnectionState connectionState;

    ChannelBackedConnection(
            final ConnectionConfiguration configuration,
            final RelativeClock relativeClock,
            final Channel channel,
            final Delineation delineation,
            final SingleConnectionEvents singleConnectionEvents
    )
    {
        this.configuration = configuration;
        this.channel = channel;
        this.delineation = delineation;
        this.singleConnectionEvents = singleConnectionEvents;
        this.connectionCommands = new ConnectionCommands(configuration.connectionId, configuration.outboundPduLimit, delineation);
        this.outgoingStream = new BufferedOutgoingStream(
                new OutgoingStream(channel, this.singleConnectionEvents, configuration.sendBufferSize),
                relativeClock,
                configuration.sendBufferSize
        );
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
                configuration.outboundPduLimit,
                delineation
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
                configuration.outboundPduLimit,
                delineation
        ));
    }

    private void handle(final CloseConnection command)
    {
        closeConnection(command.commandId(), false);
    }

    private void handle(final SendData command)
    {
        outgoingStream.sendData(command.data(), command.commandId());
        connectionState = outgoingStream.state();
    }

    private void handle(final SendMessage command)
    {
        outgoingStream.sendData(command.data(), command.commandId());
        connectionState = outgoingStream.state();
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
               "channel=" + channel +
               ", delineation=" + delineation +
               ", singleConnectionEvents=" + singleConnectionEvents +
               ", connectionCommands=" + connectionCommands +
               ", configuration=" + configuration +
               ", outgoingStream=" + outgoingStream +
               ", totalBytesReceived=" + totalBytesReceived +
               ", connectionState=" + connectionState +
               ", port=" + port +
               '}';
    }

    @Override
    public void work()
    {
        outgoingStream.work();
    }
}
