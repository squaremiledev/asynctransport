package dev.squaremile.asynctcp.transport.testfixtures;

import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public class TransportCommandSpy extends Spy<TransportCommand> implements Transport
{
    private final CapturedItems<TransportCommand> items;
    private final Transport transport;

    public TransportCommandSpy(final Transport transport)
    {
        this(new CapturedItems<>(), transport);
    }

    private TransportCommandSpy(final CapturedItems<TransportCommand> items, final Transport transport)
    {
        super(items);
        this.items = items;
        this.transport = transport;
    }

    @Override
    public void handle(final TransportCommand command)
    {
        items.add(command.copy());
        transport.handle(command);
    }

    @Override
    public void close()
    {
        transport.close();
    }

    @Override
    public <C extends TransportUserCommand> C command(final Class<C> commandType)
    {
        return transport.command(commandType);
    }

    @Override
    public <C extends ConnectionUserCommand> C command(final ConnectionId connectionId, final Class<C> commandType)
    {
        return transport.command(connectionId, commandType);
    }

    @Override
    public void work()
    {

    }
}
