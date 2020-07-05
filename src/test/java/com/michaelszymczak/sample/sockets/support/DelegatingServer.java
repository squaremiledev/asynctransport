package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.michaelszymczak.sample.sockets.ReactiveSocket;
import com.michaelszymczak.sample.sockets.Resources;

public class DelegatingServer implements FakeServer
{
    private final ServerReadiness onReady;
    private final ReactiveSocket reactiveSocket;

    public DelegatingServer(final ReactiveSocket reactiveSocket)
    {
        this.onReady = new ServerReadiness();
        this.reactiveSocket = reactiveSocket;
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
            reactiveSocket.doWork();
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
        System.out.println("Server shutting down...");
        Resources.close(reactiveSocket);
    }
}

