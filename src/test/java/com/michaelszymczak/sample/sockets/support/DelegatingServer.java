package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

import com.michaelszymczak.sample.sockets.nio.NIOBackedTransport;
import com.michaelszymczak.sample.sockets.nio.Resources;

public class DelegatingServer implements FakeServer
{
    private final Progress onReady;
    private final NIOBackedTransport transport;
    private final BooleanSupplier threadInterrupted = () -> Thread.currentThread().isInterrupted();
    private final Runnable parkForOneMs = () -> LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));

    public DelegatingServer(final NIOBackedTransport transport)
    {
        this.onReady = new Progress();
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
        transport.workUntil(threadInterrupted, parkForOneMs);
        System.out.println("Server shutting down...");
        Resources.close(transport);
    }
}

