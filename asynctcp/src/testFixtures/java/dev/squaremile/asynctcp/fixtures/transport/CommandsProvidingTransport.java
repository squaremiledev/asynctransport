package dev.squaremile.asynctcp.fixtures.transport;

import dev.squaremile.asynctcp.api.transport.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.api.transport.app.Transport;
import dev.squaremile.asynctcp.api.transport.app.TransportCommand;
import dev.squaremile.asynctcp.api.transport.app.TransportUserCommand;
import dev.squaremile.asynctcp.api.transport.commands.CloseConnection;
import dev.squaremile.asynctcp.api.transport.commands.SendData;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.api.transport.values.ConnectionIdValue;
import dev.squaremile.asynctcp.api.transport.values.Delineation;
import dev.squaremile.asynctcp.internal.transport.domain.CommandFactory;

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
