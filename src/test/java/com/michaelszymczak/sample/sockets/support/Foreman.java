package com.michaelszymczak.sample.sockets.support;

import java.util.function.BooleanSupplier;

import com.michaelszymczak.sample.sockets.api.Workman;

public class Foreman
{
    private static final int DEFAULT_TIMEOUT_MS = 1_000;

    public static void workUntil(final BooleanSupplier condition, final Workman workman)
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
