package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        public void untilCompleted(final TaskToRun taskToRunOnceInBackground)
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
            final long startTimeMs = System.currentTimeMillis();
            while (!progress.hasCompleted() && startTimeMs + 10 > System.currentTimeMillis())
            {
                try
                {
                    runBuilder.taskToKeepRunningInTheSameThread.run();
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
