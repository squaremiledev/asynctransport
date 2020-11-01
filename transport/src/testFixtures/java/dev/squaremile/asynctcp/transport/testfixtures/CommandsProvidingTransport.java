package dev.squaremile.asynctcp.transport.testfixtures;

import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.api.values.Delineation;
import dev.squaremile.asynctcp.transport.internal.domain.CommandFactory;

public class CommandsProvidingTransport implements Transport
{
    private final CommandFactory commandFactory = new CommandFactory();
    private final int capacity;
    private final Delineation delineation;

    public CommandsProvidingTransport(final int capacity, final Delineation delineation)
    {
        this.capacity = capacity;
        this.delineation = delineation;
    }

    @Override
    public <C extends TransportUserCommand> C command(final Class<C> commandType)
    {
        return commandFactory.create(commandType);
    }

    @Override
    public <C extends ConnectionUserCommand> C command(final long connectionId, final Class<C> commandType)
    {
        if (commandType.equals(CloseConnection.class))
        {
            return commandType.cast(new CloseConnection(new ConnectionIdValue(1234, connectionId)));
        }
        if (commandType.equals(SendData.class))
        {
            return commandType.cast(new SendData(new ConnectionIdValue(1234, connectionId), capacity));
        }
        if (commandType.equals(SendMessage.class))
        {
            return commandType.cast(new SendMessage(new ConnectionIdValue(1234, connectionId), capacity, delineation));
        }

        throw new UnsupportedOperationException(commandType.getSimpleName());
    }

    @Override
    public void close()
    {

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
