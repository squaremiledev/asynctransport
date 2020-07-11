package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.michaelszymczak.sample.sockets.nio.Workmen;


import static com.michaelszymczak.sample.sockets.nio.Workmen.rethrowing;
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

        public void untilCompleted(final Workmen.ThrowingBlockingWorkman taskToRunOnceInBackground)
        {
            final Progress progress = new Progress();
            final Workmen.BlockingWorkman backgroundTask = rethrowing(
                    () ->
                    {
                        taskToRunOnceInBackground.work();
                        progress.onReady();
                    });
            executorService.submit(backgroundTask::work);
            workUntil(progress::hasCompleted, taskToKeepRunningInTheSameThread);
        }
    }
}
