package dev.squaremile.asynctcp.serialization;

import org.agrona.MutableDirectBuffer;
import org.agrona.collections.Long2ObjectHashMap;


import dev.squaremile.asynctcp.domain.api.ConnectionId;
import dev.squaremile.asynctcp.domain.api.ConnectionIdValue;
import dev.squaremile.asynctcp.domain.api.Transport;
import dev.squaremile.asynctcp.domain.api.commands.CloseConnection;
import dev.squaremile.asynctcp.domain.api.commands.ConnectionCommand;
import dev.squaremile.asynctcp.domain.api.commands.TransportCommand;
import dev.squaremile.asynctcp.domain.api.events.Connected;
import dev.squaremile.asynctcp.domain.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.domain.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.domain.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.domain.api.events.TransportEvent;
import dev.squaremile.asynctcp.domain.api.events.TransportEventsListener;
import dev.squaremile.asynctcp.domain.connection.ConnectionCommands;
import dev.squaremile.asynctcp.sbe.CloseConnectionEncoder;
import dev.squaremile.asynctcp.sbe.MessageHeaderEncoder;

public class SerializingTransport implements Transport, TransportEventsListener
{
    private final MutableDirectBuffer buffer;
    private final int offset;
    private final SerializedCommandListener serializedCommandListener;
    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final CloseConnectionEncoder closeConnectionEncoder = new CloseConnectionEncoder();
    private final Long2ObjectHashMap<ConnectionCommands> connectionCommandsByConnectionId = new Long2ObjectHashMap<>();

    public SerializingTransport(final MutableDirectBuffer buffer, final int offset, final SerializedCommandListener serializedCommandListener)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.serializedCommandListener = serializedCommandListener;
    }

    @Override
    public void work()
    {

    }

    @Override
    public void close()
    {

    }

    @Override
    public <C extends TransportCommand> C command(final Class<C> commandType)
    {
        return null;
    }

    @Override
    public <C extends ConnectionCommand> C command(final ConnectionId connectionId, final Class<C> commandType)
    {
        if (!connectionCommandsByConnectionId.containsKey(connectionId.connectionId()))
        {
            throw new IllegalArgumentException("Connection id " + connectionId + " does not exist");
        }
        return connectionCommandsByConnectionId.get(connectionId.connectionId()).command(commandType);
    }

    @Override
    public void handle(final TransportCommand unknownCommand)
    {
        if (unknownCommand instanceof CloseConnection)
        {
            CloseConnection command = (CloseConnection)unknownCommand;
            closeConnectionEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(command.port())
                    .connectionId(command.connectionId())
                    .commandId(command.commandId());
            serializedCommandListener.onSerializedCommand(buffer, offset);
        }

    }

    @Override
    public void onEvent(final TransportEvent unknownEvent)
    {
        if (unknownEvent instanceof Connected)
        {
            Connected event = (Connected)unknownEvent;
            connectionCommandsByConnectionId.put(
                    event.connectionId(),
                    new ConnectionCommands(new ConnectionIdValue(event.port(), event.connectionId()), event.outboundPduLimit())
            );
        }
        if (unknownEvent instanceof ConnectionAccepted)
        {
            ConnectionAccepted event = (ConnectionAccepted)unknownEvent;
            connectionCommandsByConnectionId.put(
                    event.connectionId(),
                    new ConnectionCommands(new ConnectionIdValue(event.port(), event.connectionId()), event.outboundPduLimit())
            );
        }
        if (unknownEvent instanceof ConnectionClosed)
        {
            ConnectionClosed event = (ConnectionClosed)unknownEvent;
            connectionCommandsByConnectionId.remove(event.connectionId());
        }
        if (unknownEvent instanceof ConnectionResetByPeer)
        {
            ConnectionResetByPeer event = (ConnectionResetByPeer)unknownEvent;
            connectionCommandsByConnectionId.remove(event.connectionId());
        }
    }
}
