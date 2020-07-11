package com.michaelszymczak.sample.sockets.support;

import com.michaelszymczak.sample.sockets.nio.Workman;

public class RethrowingWorkman implements Workman
{
    private final ThrowingWorkman delegate;

    interface ThrowingWorkman
    {
        void work() throws Exception;
    }

    public static Workman rethrowing(final ThrowingWorkman delegate)
    {
        return new RethrowingWorkman(delegate);
    }

    private RethrowingWorkman(final ThrowingWorkman delegate)
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
