package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.michaelszymczak.sample.sockets.Resources;
import com.michaelszymczak.sample.sockets.Transport;

public class DelegatingServer implements FakeServer
{
    private final ServerReadiness onReady;
    private final Transport transport;

    public DelegatingServer(final Transport transport)
    {
        this.onReady = new ServerReadiness();
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
        onReady.waitUntilReady();
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

