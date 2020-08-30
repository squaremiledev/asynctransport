package dev.squaremile.asynctcp.testfitures;

import java.util.concurrent.CountDownLatch;

public class ThreadSafeProgress
{
    private final CountDownLatch serverReadyLatch;

    public ThreadSafeProgress()
    {
        this.serverReadyLatch = new CountDownLatch(1);
    }

    public boolean hasCompleted()
    {
        return serverReadyLatch.getCount() < 1;
    }

    public void onReady()
    {
        serverReadyLatch.countDown();
    }
}