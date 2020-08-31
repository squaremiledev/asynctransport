package dev.squaremile.asynctcp.nonblockingimpl;

import java.util.ArrayDeque;
import java.util.Deque;


import dev.squaremile.asynctcp.domain.api.ConnectionId;
import dev.squaremile.asynctcp.domain.api.Transport;
import dev.squaremile.asynctcp.domain.api.commands.ConnectionCommand;
import dev.squaremile.asynctcp.domain.api.commands.TransportCommand;

public class CommandsQueueTransport implements Transport
{
    private final Transport delegate;
    private final Deque<TransportCommand> commands = new ArrayDeque<>();

    public CommandsQueueTransport(final Transport delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void work()
    {
        while (!commands.isEmpty())
        {
            delegate.handle(commands.poll());
        }
        delegate.work();
    }

    @Override
    public void handle(final TransportCommand command)
    {
        // TODO: find a way of doing it without generating garbage
        commands.add(command.copy());
    }

    @Override
    public void close()
    {
        delegate.close();
    }

    @Override
    public <C extends TransportCommand> C command(final Class<C> commandType)
    {
        return delegate.command(commandType);
    }

    @Override
    public <C extends ConnectionCommand> C command(final ConnectionId connectionId, final Class<C> commandType)
    {
        return delegate.command(connectionId, commandType);
    }
}
