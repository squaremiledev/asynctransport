package dev.squaremile.asynctcp.serialization.internal.delineation;

import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportOnDuty;
import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.events.TransportCommandFailed;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

public class DelineationValidatingTransport implements TransportOnDuty
{
    private final DelineationHandlerFactory delineationHandlerFactory = new DelineationHandlerFactory();
    private final EventListener eventListener;
    private final TransportOnDuty delegate;

    public DelineationValidatingTransport(final EventListener eventListener, final TransportOnDuty delegate)
    {
        this.eventListener = eventListener;
        this.delegate = delegate;
    }

    @Override
    public void close()
    {
        delegate.close();
    }

    @Override
    public <C extends TransportUserCommand> C command(final Class<C> commandType)
    {
        return delegate.command(commandType);
    }

    @Override
    public <C extends ConnectionUserCommand> C command(final ConnectionId connectionId, final Class<C> commandType)
    {
        return delegate.command(connectionId, commandType);
    }

    @Override
    public void handle(final TransportCommand command)
    {
        if (command instanceof Listen)
        {
            Delineation delineation = ((Listen)command).delineation();
            if (!delineationHandlerFactory.isSupported(delineation))
            {
                eventListener.onEvent(new TransportCommandFailed(command, "Unsupported delineation " + delineation));
                return;
            }
        }
        if (command instanceof Connect)
        {
            Delineation delineation = ((Connect)command).delineation();
            if (!delineationHandlerFactory.isSupported(delineation))
            {
                eventListener.onEvent(new TransportCommandFailed(command, "Unsupported delineation " + delineation));
                return;
            }
        }
        delegate.handle(command);
    }

    @Override
    public void work()
    {
        delegate.work();
    }
}
