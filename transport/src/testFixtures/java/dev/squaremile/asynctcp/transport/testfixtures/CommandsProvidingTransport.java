package dev.squaremile.asynctcp.transport.testfixtures;

import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportOnDuty;
import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.internal.domain.CommandFactory;

public class CommandsProvidingTransport implements TransportOnDuty
{
    private final CommandFactory commandFactory = new CommandFactory();
    private int capacity;

    public CommandsProvidingTransport(final int capacity)
    {
        this.capacity = capacity;
    }

    @Override
    public void close()
    {

    }

    @Override
    public <C extends TransportUserCommand> C command(final Class<C> commandType)
    {
        return commandFactory.create(commandType);
    }

    @Override
    public <C extends ConnectionUserCommand> C command(final ConnectionId connectionId, final Class<C> commandType)
    {
        if (commandType.equals(CloseConnection.class))
        {
            return commandType.cast(new CloseConnection(connectionId));
        }
        if (commandType.equals(SendData.class))
        {
            return commandType.cast(new SendData(connectionId, capacity));
        }
        if (commandType.equals(SendMessage.class))
        {
            return commandType.cast(new SendMessage(connectionId, capacity));
        }

        throw new UnsupportedOperationException(commandType.getSimpleName());
    }

    @Override
    public void work()
    {

    }

    @Override
    public void handle(final TransportCommand command)
    {
    }
}
