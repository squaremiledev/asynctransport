package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.michaelszymczak.sample.sockets.nio.Workmen;


import static com.michaelszymczak.sample.sockets.support.Foreman.workUntil;

public class BackgroundRunner
{
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public RunBuilder keepRunning(Workmen.ThrowingNonBlockingWorkman taskToKeepRunningInTheSameThread)
    {
        return new RunBuilder(taskToKeepRunningInTheSameThread);
    }

    public class RunBuilder
    {
        final Workmen.ThrowingNonBlockingWorkman taskToKeepRunningInTheSameThread;

        RunBuilder(final Workmen.ThrowingNonBlockingWorkman taskToKeepRunningInTheSameThread)
        {
            this.taskToKeepRunningInTheSameThread = taskToKeepRunningInTheSameThread;
        }

        public void untilCompletedWithin(final Workmen.ThrowingBlockingWorkman taskToRunOnceInBackground, final int timeoutMs)
        {
            final Progress progress = new Progress();
            final Workmen.BlockingWorkman backgroundTask = Workmen.rethrowing((Workmen.ThrowingBlockingWorkman)() ->
            {
                taskToRunOnceInBackground.work();
                progress.onReady();
            });
            executorService.submit(backgroundTask::work);
            workUntil(taskToKeepRunningInTheSameThread, !progress.hasCompleted(), timeoutMs);
        }
    }
}
