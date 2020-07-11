package com.michaelszymczak.sample.sockets.support;

import com.michaelszymczak.sample.sockets.nio.Workmen;


import static com.michaelszymczak.sample.sockets.support.RethrowingWorkman.rethrowing;

public class Foreman
{

    public static void workUntil(final Workmen.ThrowingNonBlockingWorkman workman, final boolean condition, final int timeoutMs)
    {
        workUntil(rethrowing(workman), condition, timeoutMs);
    }

    public static void workUntil(final Workmen.NonBlockingWorkman workman, final boolean condition, final int timeoutMs)
    {
        final long startTime = System.currentTimeMillis();
        while (condition && startTime + timeoutMs > System.currentTimeMillis())
        {
            workman.work();
        }
    }
}
