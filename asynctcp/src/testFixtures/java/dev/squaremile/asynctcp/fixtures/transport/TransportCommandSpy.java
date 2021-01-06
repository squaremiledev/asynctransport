package dev.squaremile.asynctcp.fixtures.transport;

import dev.squaremile.asynctcp.api.transport.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.api.transport.app.Transport;
import dev.squaremile.asynctcp.api.transport.app.TransportCommand;
import dev.squaremile.asynctcp.api.transport.app.TransportUserCommand;

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
    public <C extends ConnectionUserCommand> C command(final long connectionId, final Class<C> commandType)
    {
        return transport.command(connectionId, commandType);
    }

    @Override
    public void work()
    {

    }
}
