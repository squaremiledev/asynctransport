package dev.squaremile.asynctcp.serialization;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.domain.api.ConnectionId;
import dev.squaremile.asynctcp.domain.api.Transport;
import dev.squaremile.asynctcp.domain.api.commands.CloseConnection;
import dev.squaremile.asynctcp.domain.api.commands.ConnectionCommand;
import dev.squaremile.asynctcp.domain.api.commands.TransportCommand;
import dev.squaremile.asynctcp.sbe.CloseConnectionEncoder;
import dev.squaremile.asynctcp.sbe.MessageHeaderEncoder;

public class SerializingTransport implements Transport
{
    private final MutableDirectBuffer buffer;
    private final int offset;
    private final SerializedCommandListener serializedCommandListener;
    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final CloseConnectionEncoder closeConnectionEncoder = new CloseConnectionEncoder();

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
        return null;
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
}
