package dev.squaremile.asynctcp.internal.serialization.delineation;

import dev.squaremile.asynctcp.api.transport.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.api.transport.app.EventListener;
import dev.squaremile.asynctcp.api.transport.app.Transport;
import dev.squaremile.asynctcp.api.transport.app.TransportCommand;
import dev.squaremile.asynctcp.api.transport.app.TransportUserCommand;
import dev.squaremile.asynctcp.api.transport.commands.Connect;
import dev.squaremile.asynctcp.api.transport.commands.Listen;
import dev.squaremile.asynctcp.api.transport.events.TransportCommandFailed;
import dev.squaremile.asynctcp.api.transport.values.Delineation;

public class DelineationValidatingTransport implements Transport
{
    private final DelineationHandlerFactory delineationHandlerFactory = new DelineationHandlerFactory();
    private final EventListener eventListener;
    private final Transport delegate;

    public DelineationValidatingTransport(final EventListener eventListener, final Transport delegate)
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
    public <C extends ConnectionUserCommand> C command(final long connectionId, final Class<C> commandType)
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

    @Override
    public void onStart()
    {
        delegate.onStart();
    }

    @Override
    public void onStop()
    {
        delegate.onStop();
    }
}
