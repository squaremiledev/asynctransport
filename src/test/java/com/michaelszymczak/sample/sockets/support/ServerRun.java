package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class ServerRun implements AutoCloseable
{

    private final Future<?> serverTask;
    private final FakeServer server;

    private ServerRun(final Future<?> serverTask, final FakeServer server)
    {
        this.serverTask = serverTask;
        this.server = server;
    }

    public static ServerRun startServer(final FakeServer server) throws InterruptedException, ExecutionException
    {
        Future<?> serverTask = newSingleThreadExecutor().submit(server::startServer);
        server.waitUntilReady();
        if (serverTask.isDone())
        {
            serverTask.get();
        }
        return new ServerRun(serverTask, server);
    }

    public int serverPort()
    {
        return server.port();
    }

    @Override
    public void close()
    {
        stopServer();
    }

    private void stopServer()
    {
        serverTask.cancel(true);
    }
}
