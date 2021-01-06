package dev.squaremile.asynctcp.internal.transport.domain.connection;

import dev.squaremile.asynctcp.api.transport.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.api.transport.commands.CloseConnection;
import dev.squaremile.asynctcp.api.transport.commands.SendData;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.api.transport.values.ConnectionIdValue;
import dev.squaremile.asynctcp.api.transport.values.Delineation;

public class ConnectionCommands
{
    private final SendData sendDataCommand;
    private final SendMessage sendMessageCommand;
    private final CloseConnection closeConnection;

    public ConnectionCommands(final ConnectionIdValue connectionId, final int initialSenderBufferSize, final Delineation delineation)
    {
        this.sendDataCommand = new SendData(connectionId, initialSenderBufferSize);
        this.sendMessageCommand = new SendMessage(connectionId, initialSenderBufferSize, delineation);
        this.closeConnection = new CloseConnection(connectionId);
    }

    public <C extends ConnectionUserCommand> C command(final Class<C> commandType)
    {
        if (commandType.equals(SendData.class))
        {
            return commandType.cast(sendDataCommand.reset());
        }
        if (commandType.equals(SendMessage.class))
        {
            return commandType.cast(sendMessageCommand.reset());
        }
        if (commandType.equals(CloseConnection.class))
        {
            return commandType.cast(closeConnection);
        }
        throw new IllegalArgumentException(commandType.getSimpleName());
    }
}
