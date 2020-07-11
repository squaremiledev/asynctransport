package com.michaelszymczak.sample.sockets.support;

import com.michaelszymczak.sample.sockets.nio.Workman;

public class Foreman
{

    public static void workUntil(final Workman workman, final boolean condition, final int timeoutMs)
    {
        final long startTime = System.currentTimeMillis();
        while (condition && startTime + timeoutMs > System.currentTimeMillis())
        {
            workman.work();
        }
    }
}
