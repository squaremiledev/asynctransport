package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;

public class BackgroundRunner
{
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static BooleanSupplier completed(final ThrowingRunnable taskToRunOnceInBackground)
    {
        final ThreadSafeProgress progress = new ThreadSafeProgress();
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
        return progress::hasCompleted;
    }

    public interface ThrowingRunnable
    {
        void run() throws Exception;
    }
}
