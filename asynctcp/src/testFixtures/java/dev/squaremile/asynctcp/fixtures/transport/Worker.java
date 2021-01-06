package dev.squaremile.asynctcp.fixtures.transport;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;


import static dev.squaremile.asynctcp.fixtures.transport.ThrowWhenTimedOutBeforeMeeting.DEFAULT_TIMEOUT_MS;
import static dev.squaremile.asynctcp.fixtures.transport.ThrowWhenTimedOutBeforeMeeting.timeoutOr;
import static java.util.concurrent.locks.LockSupport.parkNanos;

public class Worker
{
    public static BooleanSupplier noExceptionAnd(final BooleanSupplier condition)
    {
        return () ->
        {
            try
            {
                Thread.sleep(100);
                return condition.getAsBoolean();
            }
            catch (Exception e)
            {
                return false;
            }
        };
    }

    public static void runUntil(final BooleanSupplier stopCondition)
    {
        runUntil(DEFAULT_TIMEOUT_MS, stopCondition);
    }

    public static void runUntil(final int timeoutMs, final BooleanSupplier stopCondition)
    {
        final BooleanSupplier abort = timeoutOr(timeoutMs, stopCondition);
        while (!abort.getAsBoolean())
        {
            parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
    }
}