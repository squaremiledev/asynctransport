package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.CountDownLatch;

class ServerReadiness
{
    private final CountDownLatch serverReadyLatch;

    ServerReadiness()
    {
        this.serverReadyLatch = new CountDownLatch(1);
    }

    void waitUntilReady()
    {
        try
        {
            serverReadyLatch.await();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    void onReady()
    {
        serverReadyLatch.countDown();
    }
}
