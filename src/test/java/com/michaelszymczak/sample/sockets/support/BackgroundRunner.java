package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import static com.michaelszymczak.sample.sockets.support.Foreman.workUntil;
import static com.michaelszymczak.sample.sockets.support.RethrowingWorkman.rethrowing;

public class BackgroundRunner
{
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public RunBuilder keepRunning(TaskToRun taskToKeepRunningInTheSameThread)
    {
        return new RunBuilder(BackgroundRunner.this.executorService, taskToKeepRunningInTheSameThread, null);
    }

    public interface TaskToRun
    {
        void run() throws Exception;
    }

    public static class RunBuilder
    {
        final TaskToRun taskToKeepRunningInTheSameThread;
        final TaskToRun taskToRunOnceInBackground;
        private ExecutorService executorService;

        public RunBuilder(final ExecutorService executorService, final TaskToRun taskToKeepRunningInTheSameThread, final TaskToRun taskToRunOnceInBackground)
        {
            this.executorService = executorService;
            this.taskToKeepRunningInTheSameThread = taskToKeepRunningInTheSameThread;
            this.taskToRunOnceInBackground = taskToRunOnceInBackground;
        }

        public void untilCompletedWithin(final TaskToRun taskToRunOnceInBackground, final int timeoutMs)
        {
            final RunBuilder runBuilder = new RunBuilder(executorService, taskToKeepRunningInTheSameThread, taskToRunOnceInBackground);
            final Progress progress = new Progress();
            executorService.submit(() ->
                                   {
                                       try
                                       {
                                           runBuilder.taskToRunOnceInBackground.run();
                                           progress.onReady();
                                       }
                                       catch (Exception e)
                                       {
                                           throw new RuntimeException(e);
                                       }
                                   });
            workUntil(rethrowing(runBuilder.taskToKeepRunningInTheSameThread::run), !progress.hasCompleted(), timeoutMs);
        }
    }
}
