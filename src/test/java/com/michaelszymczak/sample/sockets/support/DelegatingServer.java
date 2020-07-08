package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.michaelszymczak.sample.sockets.impl.NIOBackedTransport;
import com.michaelszymczak.sample.sockets.impl.Resources;

public class DelegatingServer implements FakeServer
{
    private final Progress onReady;
    private final NIOBackedTransport transport;

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
        while (!Thread.currentThread().isInterrupted())
        {
            transport.doWork();
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
        System.out.println("Server shutting down...");
        Resources.close(transport);
    }
}

