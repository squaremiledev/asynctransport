package dev.squaremile.asynctcp.fixtures.transport;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;


import dev.squaremile.asynctcp.api.transport.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.api.transport.app.Transport;
import dev.squaremile.asynctcp.api.transport.app.TransportCommand;
import dev.squaremile.asynctcp.api.transport.app.TransportCommandHandler;
import dev.squaremile.asynctcp.api.transport.app.TransportEventsListener;
import dev.squaremile.asynctcp.api.transport.app.TransportUserCommand;
import dev.squaremile.asynctcp.internal.transport.domain.StatusEventListener;
import dev.squaremile.asynctcp.internal.transport.nonblockingimpl.NonBlockingTransport;

import static dev.squaremile.asynctcp.fixtures.transport.ThrowWhenTimedOutBeforeMeeting.timeoutOr;
import static java.util.concurrent.locks.LockSupport.parkNanos;

public class TestableTransport<E extends TransportEventsListener> implements Transport
{
    private final Transport delegate;
    private final E events;

    TestableTransport(final E events, final StatusEventListener statusEventListener)
    {
        this.events = events;
        try
        {
            this.delegate = new NonBlockingTransport(new DelegatingEventListener(events, statusEventListener), TransportCommandHandler.NO_HANDLER, System::currentTimeMillis, "");
        }
        catch (Exception e)
        {
            throw new RuntimeException();
        }
    }

    public E events()
    {
        return events;
    }

    public void workTimes(int iterations)
    {
        for (int i = 0; i < iterations; i++)
        {
            work();
        }
    }

    public void workUntil(final BooleanSupplier stopCondition)
    {
        final BooleanSupplier abort = timeoutOr(stopCondition);
        while (!abort.getAsBoolean())
        {
            work();
            parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
    }

    @Override
    public void work()
    {
        delegate.work();
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
    public void close()
    {
        delegate.close();
    }

    @Override
    public void handle(final TransportCommand command)
    {
        work();
        delegate.handle(command);
    }
}
