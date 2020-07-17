package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import com.michaelszymczak.sample.sockets.api.Transport;
import com.michaelszymczak.sample.sockets.api.commands.TransportCommand;
import com.michaelszymczak.sample.sockets.api.events.StatusEventListener;
import com.michaelszymczak.sample.sockets.api.events.TransportEventsListener;
import com.michaelszymczak.sample.sockets.nio.NIOBackedTransport;


import static com.michaelszymczak.sample.sockets.support.ThrowWhenTimedOutBeforeMeeting.timeoutOr;
import static java.util.concurrent.locks.LockSupport.parkNanos;

public class TestableTransport<E extends TransportEventsListener> implements Transport
{
    private final NIOBackedTransport delegate;
    private final E events;

    public TestableTransport(final E events, final StatusEventListener statusEventListener)
    {
        this.events = events;
        try
        {
            this.delegate = new NIOBackedTransport(events, statusEventListener);
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
        delegate.handle(command);
    }

    @Override
    public void close()
    {
        delegate.close();
    }
}
