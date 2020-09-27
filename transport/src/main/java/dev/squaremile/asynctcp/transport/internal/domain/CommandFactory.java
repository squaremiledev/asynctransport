package dev.squaremile.asynctcp.transport.internal.domain;

import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.app.ConnectionCommand;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.commands.StopListening;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public class CommandFactory
{
    public <C extends ConnectionCommand> C create(final ConnectionId connectionId, final Class<C> commandType)
    {
        if (commandType.equals(ReadData.class))
        {
            return commandType.cast(new ReadData(connectionId));
        }
        if (commandType.equals(SendData.class))
        {
            return commandType.cast(new SendData(connectionId, 0));
        }
        if (commandType.equals(CloseConnection.class))
        {
            return commandType.cast(new CloseConnection(connectionId));
        }
        if (commandType.equals(NoOpCommand.class))
        {
            return commandType.cast(new NoOpCommand(connectionId));
        }
        throw new IllegalArgumentException(commandType.getSimpleName());
    }

    public <C extends TransportCommand> C create(final Class<C> commandType)
    {
        if (commandType.equals(Listen.class))
        {
            return commandType.cast(new Listen());
        }
        if (commandType.equals(Connect.class))
        {
            return commandType.cast(new Connect());
        }
        if (commandType.equals(StopListening.class))
        {
            return commandType.cast(new StopListening());
        }
        throw new IllegalArgumentException(commandType.getSimpleName());
    }
}
