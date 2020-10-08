package dev.squaremile.asynctcp.serialization.internal;

import org.agrona.MutableDirectBuffer;
import org.agrona.collections.Long2ObjectHashMap;


import dev.squaremile.asynctcp.sbe.CloseConnectionEncoder;
import dev.squaremile.asynctcp.sbe.ConnectEncoder;
import dev.squaremile.asynctcp.sbe.ListenEncoder;
import dev.squaremile.asynctcp.sbe.MessageHeaderEncoder;
import dev.squaremile.asynctcp.sbe.SendDataEncoder;
import dev.squaremile.asynctcp.sbe.SendMessageEncoder;
import dev.squaremile.asynctcp.sbe.StopListeningEncoder;
import dev.squaremile.asynctcp.serialization.api.SerializedCommandListener;
import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.commands.StopListening;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.internal.domain.CommandFactory;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionCommands;

public class SerializingTransport implements Transport, EventListener
{
    private final MutableDirectBuffer buffer;
    private final int offset;
    private final SerializedCommandListener serializedCommandListener;
    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final CloseConnectionEncoder closeConnectionEncoder = new CloseConnectionEncoder();
    private final ListenEncoder listenEncoder = new ListenEncoder();
    private final StopListeningEncoder stopListeningEncoder = new StopListeningEncoder();
    private final SendDataEncoder sendDataEncoder = new SendDataEncoder();
    private final SendMessageEncoder sendMessageEncoder = new SendMessageEncoder();
    private final ConnectEncoder connectEncoder = new ConnectEncoder();
    private final Long2ObjectHashMap<ConnectionCommands> connectionCommandsByConnectionId = new Long2ObjectHashMap<>();
    private final CommandFactory commandFactory = new CommandFactory();
    private final Listen listenCommand;
    private final StopListening stopListeningCommand;
    private final Connect connectCommand;

    public SerializingTransport(final MutableDirectBuffer buffer, final int offset, final SerializedCommandListener serializedCommandListener)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.serializedCommandListener = serializedCommandListener;
        this.listenCommand = commandFactory.create(Listen.class);
        this.stopListeningCommand = commandFactory.create(StopListening.class);
        this.connectCommand = commandFactory.create(Connect.class);
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
    public <C extends TransportUserCommand> C command(final Class<C> commandType)
    {
        if (commandType.equals(Listen.class))
        {
            return commandType.cast(listenCommand);
        }
        if (commandType.equals(Connect.class))
        {
            return commandType.cast(connectCommand);
        }
        if (commandType.equals(StopListening.class))
        {
            return commandType.cast(stopListeningCommand);
        }
        return commandFactory.create(commandType);
    }

    @Override
    public <C extends ConnectionUserCommand> C command(final ConnectionId connectionId, final Class<C> commandType)
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
            serializedCommandListener.onSerialized(buffer, offset, headerEncoder.encodedLength() + closeConnectionEncoder.encodedLength());
        }
        else if (unknownCommand instanceof Connect)
        {
            Connect command = (Connect)unknownCommand;
            connectEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .remotePort(command.remotePort())
                    .commandId(command.commandId())
                    .timeoutMs(command.timeoutMs())
                    .delineationType(DelineationTypeMapping.toWire(command.delineation().type()))
                    .delineationKnownLength(command.delineation().knownLength())
                    .delineationPattern(command.delineation().pattern())
                    .remoteHost(command.remoteHost());
            serializedCommandListener.onSerialized(buffer, offset, headerEncoder.encodedLength() + connectEncoder.encodedLength());
        }
        else if (unknownCommand instanceof Listen)
        {
            Listen command = (Listen)unknownCommand;
            listenEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(command.port())
                    .commandId(command.commandId())
                    .delineationType(DelineationTypeMapping.toWire(command.delineation().type()))
                    .delineationKnownLength(command.delineation().knownLength())
                    .delineationPattern(command.delineation().pattern());
            serializedCommandListener.onSerialized(buffer, offset, headerEncoder.encodedLength() + listenEncoder.encodedLength());
        }
        else if (unknownCommand instanceof StopListening)
        {
            StopListening command = (StopListening)unknownCommand;
            stopListeningEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(command.port())
                    .commandId(command.commandId());
            serializedCommandListener.onSerialized(buffer, offset, headerEncoder.encodedLength() + stopListeningEncoder.encodedLength());
        }
        else if (unknownCommand instanceof SendData)
        {
            SendData command = (SendData)unknownCommand;
            final SendDataEncoder encoder = this.sendDataEncoder;

            encoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(command.port())
                    .connectionId(command.connectionId())
                    .commandId(command.commandId())
                    .capacity(command.capacity());

            encoder.data().length(command.length());
            encoder.data().buffer().putBytes(encoder.data().offset() + encoder.data().encodedLength(), command.buffer(), command.offset(), command.length());
            serializedCommandListener.onSerialized(this.buffer, this.offset, headerEncoder.encodedLength() + encoder.encodedLength());
        }
        else if (unknownCommand instanceof SendMessage)
        {
            SendMessage command = (SendMessage)unknownCommand;
            final SendMessageEncoder encoder = this.sendMessageEncoder;

            encoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(command.port())
                    .connectionId(command.connectionId())
                    .commandId(command.commandId());

            encoder.data().length(command.length());
            encoder.data().buffer().putBytes(encoder.data().offset() + encoder.data().encodedLength(), command.buffer(), command.offset(), command.length());
            serializedCommandListener.onSerialized(this.buffer, this.offset, headerEncoder.encodedLength() + encoder.encodedLength());
        }
        else
        {
            throw new UnsupportedOperationException(unknownCommand.toString());
        }

    }

    @Override
    public void onEvent(final Event unknownEvent)
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
