package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

import com.michaelszymczak.sample.sockets.api.Transport;
import com.michaelszymczak.sample.sockets.nio.Resources;

public class DelegatingServer implements FakeServer
{
    private final ThreadSafeProgress onReady;
    private final Transport transport;
    private final BooleanSupplier threadInterrupted = () -> Thread.currentThread().isInterrupted();
    private final Runnable parkForOneMs = () -> LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));

    public DelegatingServer(final Transport transport)
    {
        this.onReady = new ThreadSafeProgress();
        this.transport = transport;
    }

    @Override
    public int port()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void waitUntilReady()
    {
        onReady.blockUntilReady();
    }


    @Override
    public void startServer()
    {
        onReady.onReady();
        while (!threadInterrupted.getAsBoolean())
        {
            transport.work();
        }
        parkForOneMs.run();
        System.out.println("Server shutting down...");
        Resources.close(transport);
    }
}

