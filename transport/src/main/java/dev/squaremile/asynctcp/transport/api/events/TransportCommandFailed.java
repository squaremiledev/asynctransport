package dev.squaremile.asynctcp.transport.api.events;

import dev.squaremile.asynctcp.transport.api.app.CommandFailed;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;

public class TransportCommandFailed implements CommandFailed
{
    private final int port;
    private final long commandId;
    private final String details;
    private final String commandType;

    public TransportCommandFailed(final TransportCommand command, final String details)
    {
        this(command.port(), command.commandId(), details, command.getClass());
    }

    public TransportCommandFailed(final int port, final long commandId, final String details, final Class<? extends TransportCommand> commandType)
    {
        this(port, commandId, details, commandType.getSimpleName());
    }

    public TransportCommandFailed(final int port, final long commandId, final String details, final String commandType)
    {
        this.port = port;
        this.commandId = commandId;
        this.details = details;
        this.commandType = commandType;
    }

    @Override
    public int port()
    {
        return port;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    @Override
    public String details()
    {
        return details;
    }

    public String commandType()
    {
        return commandType;
    }

    @Override
    public String toString()
    {
        return "TransportCommandFailed{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", details='" + details + '\'' +
               ", commandType='" + commandType + '\'' +
               '}';
    }

    @Override
    public TransportEvent copy()
    {
        return new TransportCommandFailed(port, commandId, details, commandType);
    }
}
