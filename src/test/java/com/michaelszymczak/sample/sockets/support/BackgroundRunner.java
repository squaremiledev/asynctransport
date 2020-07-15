package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.michaelszymczak.sample.sockets.api.Workman;

public class BackgroundRunner
{
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public RunBuilder keepRunning(Workman taskToKeepRunningInTheSameThread)
    {
        return new RunBuilder(taskToKeepRunningInTheSameThread);
    }

    public interface ThrowingRunnable
    {
        void run() throws Exception;
    }

    public class RunBuilder
    {
        final Workman taskToKeepRunningInTheSameThread;

        RunBuilder(final Workman taskToKeepRunningInTheSameThread)
        {
            this.taskToKeepRunningInTheSameThread = taskToKeepRunningInTheSameThread;
        }

        public void untilCompleted(final ThrowingRunnable taskToRunOnceInBackground)
        {
            final Progress progress = new Progress();
            executorService.submit(() ->
                                   {
                                       try
                                       {
                                           taskToRunOnceInBackground.run();
                                           progress.onReady();
                                       }
                                       catch (Exception e)
                                       {
                                           throw new RuntimeException(e);
                                       }
                                   });
            taskToKeepRunningInTheSameThread.workUntil(progress::hasCompleted);
        }
    }
}
