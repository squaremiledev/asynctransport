package com.michaelszymczak.sample.sockets.support;

import com.michaelszymczak.sample.sockets.nio.Workmen;

public class RethrowingWorkman implements Workmen.NonBlockingWorkman
{
    private final Workmen.ThrowingNonBlockingWorkman delegate;

    public static Workmen.NonBlockingWorkman rethrowing(final Workmen.ThrowingNonBlockingWorkman delegate)
    {
        return new RethrowingWorkman(delegate);
    }

    private RethrowingWorkman(final Workmen.ThrowingNonBlockingWorkman delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void work()
    {
        try
        {
            delegate.work();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
