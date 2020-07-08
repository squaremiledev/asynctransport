package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundRunner
{
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Progress run(final TaskToRun taskToRun)
    {
        final Progress progress = new Progress();
        executorService.submit(() ->
                               {
                                   try
                                   {
                                       taskToRun.run();
                                       progress.onReady();
                                   }
                                   catch (Exception e)
                                   {
                                       throw new RuntimeException(e);
                                   }
                               });
        return progress;
    }

    public interface TaskToRun
    {
        void run() throws Exception;
    }
}
