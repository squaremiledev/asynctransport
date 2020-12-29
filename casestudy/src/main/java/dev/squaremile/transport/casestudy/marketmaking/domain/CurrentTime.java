package dev.squaremile.transport.casestudy.marketmaking.domain;

import java.util.concurrent.TimeUnit;

import org.agrona.concurrent.SystemNanoClock;

public class CurrentTime
{
    public static long currentTime()
    {
        return SystemNanoClock.INSTANCE.nanoTime();
    }

    public static long timeFromMs(long timeMs)
    {
        return TimeUnit.MILLISECONDS.toNanos(timeMs);
    }
}
