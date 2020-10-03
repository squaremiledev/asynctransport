package dev.squaremile.asynctcp.transport.testfixtures;

import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.internal.domain.CommandFactory;

public class CommandsProvidingTransport implements Transport
{
    private final CommandFactory commandFactory = new CommandFactory();

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
        return commandFactory.create(connectionId, commandType);
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
