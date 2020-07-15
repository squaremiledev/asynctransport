package com.michaelszymczak.sample.sockets.support;

import java.util.function.BooleanSupplier;

import com.michaelszymczak.sample.sockets.api.Transport;
import com.michaelszymczak.sample.sockets.api.commands.TransportCommand;
import com.michaelszymczak.sample.sockets.api.events.TransportEventsListener;
import com.michaelszymczak.sample.sockets.nio.NIOBackedTransport;

public class TestedTransport implements Transport
{
    private final NIOBackedTransport delegate;

    public TestedTransport(final TransportEventsListener events)
    {
        try
        {
            this.delegate = new NIOBackedTransport(events);
        }
        catch (Exception e)
        {
            throw new RuntimeException();
        }
    }

    public void workUntil(final BooleanSupplier stopCondition, final Runnable taskAfterIteration)
    {
        while (!stopCondition.getAsBoolean())
        {
            work();
        }
        taskAfterIteration.run();
    }

    public void workUntil(final BooleanSupplier stopCondition)
    {
        workUntil(stopCondition, () ->
        {

        });
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
