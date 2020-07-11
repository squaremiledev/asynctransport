package com.michaelszymczak.sample.sockets.support;

import java.util.function.BooleanSupplier;

import com.michaelszymczak.sample.sockets.nio.Workmen;


import static com.michaelszymczak.sample.sockets.support.RethrowingWorkman.rethrowing;

public class Foreman
{
    private static final int DEFAULT_TIMEOUT_MS = 1000;

    public static void workUntil(final BooleanSupplier condition, final Workmen.ThrowingNonBlockingWorkman workman)
    {
        workUntil(condition, rethrowing(workman));
    }

    public static void workUntil(final BooleanSupplier condition, final Workmen.NonBlockingWorkman workman)
    {
        final long startTime = System.currentTimeMillis();
        while (!condition.getAsBoolean() && !timedOut(startTime))
        {
            workman.work();
        }
        if (!condition.getAsBoolean())
        {
            throw new RuntimeException("Not completed within " + DEFAULT_TIMEOUT_MS + "ms");
        }
    }

    private static boolean timedOut(final long startTime)
    {
        return startTime + DEFAULT_TIMEOUT_MS <= System.currentTimeMillis();
    }
}
