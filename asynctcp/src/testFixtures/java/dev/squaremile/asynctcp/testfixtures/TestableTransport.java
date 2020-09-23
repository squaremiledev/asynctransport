package dev.squaremile.asynctcp.testfixtures;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;


import dev.squaremile.asynctcp.domain.api.ConnectionId;
import dev.squaremile.asynctcp.domain.api.Transport;
import dev.squaremile.asynctcp.domain.api.commands.ConnectionUserCommand;
import dev.squaremile.asynctcp.domain.api.commands.TransportCommand;
import dev.squaremile.asynctcp.domain.api.events.DelegatingEventListener;
import dev.squaremile.asynctcp.domain.api.events.StatusEventListener;
import dev.squaremile.asynctcp.domain.api.events.TransportEventsListener;
import dev.squaremile.asynctcp.nonblockingimpl.NonBlockingTransport;

import static dev.squaremile.asynctcp.testfixtures.ThrowWhenTimedOutBeforeMeeting.timeoutOr;
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
            this.delegate = new NonBlockingTransport(new DelegatingEventListener(events, statusEventListener), System::currentTimeMillis, "");
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
    public void handle(final TransportCommand command)
    {
        work();
        delegate.handle(command);
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
    public <C extends ConnectionUserCommand> C command(final ConnectionId connectionId, final Class<C> commandType)
    {
        return delegate.command(connectionId, commandType);
    }
}
