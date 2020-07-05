package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.michaelszymczak.sample.sockets.ReactiveSocket;
import com.michaelszymczak.sample.sockets.Resources;

public class DelegatingServer implements FakeServer
{
    private final int serverPort;
    private final ServerReadiness onReady;
    private final ReactiveSocket reactiveSocket;

    public DelegatingServer(final int serverPort)
    {
        this.serverPort = FreePort.freePort(serverPort);
        this.onReady = new ServerReadiness();
        this.reactiveSocket = new ReactiveSocket();
    }

    @Override
    public int port()
    {
        return serverPort;
    }

    @Override
    public void waitUntilReady()
    {
        onReady.waitUntilReady();
    }


    @Override
    public void startServer()
    {
        reactiveSocket.accept(serverPort);
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

