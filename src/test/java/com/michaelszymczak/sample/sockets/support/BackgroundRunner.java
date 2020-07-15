package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;


import static com.michaelszymczak.sample.sockets.support.ThrowWhenTimedOutBeforeMeeting.timeoutOr;

public class BackgroundRunner
{
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public BooleanSupplier completed(final ThrowingRunnable taskToRunOnceInBackground)
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
        return timeoutOr(progress::hasCompleted);
    }

    public interface ThrowingRunnable
    {
        void run() throws Exception;
    }
}
