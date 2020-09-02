package dev.squaremile.asynctcp.testfixtures;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;


import static dev.squaremile.asynctcp.testfixtures.ThrowWhenTimedOutBeforeMeeting.timeoutOr;
import static java.util.concurrent.locks.LockSupport.parkNanos;

public class Worker
{
    public static void runUntil(final BooleanSupplier stopCondition)
    {
        final BooleanSupplier abort = timeoutOr(stopCondition);
        while (!abort.getAsBoolean())
        {
            parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
    }

    public static void runWithoutTimeoutUntil(final BooleanSupplier stopCondition)
    {
        while (!stopCondition.getAsBoolean())
        {
            parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
    }
}
