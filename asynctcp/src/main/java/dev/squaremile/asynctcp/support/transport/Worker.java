package dev.squaremile.asynctcp.support.transport;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;


import static java.util.concurrent.locks.LockSupport.parkNanos;

public class Worker
{
    private static final int DEFAULT_TIMEOUT_MS = 1_000;

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
        long startTime = System.currentTimeMillis();
        final BooleanSupplier abort = () ->
        {
            final boolean conditionMet = stopCondition.getAsBoolean();
            if (conditionMet)
            {
                return true;
            }
            final boolean hasTimedOut = startTime + timeoutMs <= System.currentTimeMillis();
            if (!hasTimedOut)
            {
                return false;
            }
            throw new RuntimeException("Not completed within " + timeoutMs + "ms");
        };

        while (!abort.getAsBoolean())
        {
            parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
    }
}
