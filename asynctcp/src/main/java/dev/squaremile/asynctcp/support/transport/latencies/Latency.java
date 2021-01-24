package dev.squaremile.asynctcp.support.transport.latencies;

import java.util.concurrent.TimeUnit;

public class Latency
{
    long iteration;
    int pointA;
    int pointB;
    long pointANanos;
    long pointBNanos;
    long latencyNanos = Long.MIN_VALUE;

    public Latency set(final long iteration, final int pointA, final int pointB, final long pointANanos, final long pointBNanos)
    {
        this.iteration = iteration;
        this.pointA = pointA;
        this.pointB = pointB;
        this.pointANanos = pointANanos;
        this.pointBNanos = pointBNanos;
        this.latencyNanos = pointBNanos - pointANanos;
        return this;
    }

    public long iteration()
    {
        return iteration;
    }

    public int pointFrom()
    {
        return pointA;
    }

    public int pointTo()
    {
        return pointB;
    }

    public long latencyNanos()
    {
        return latencyNanos;
    }

    public long latencyMicroseconds()
    {
        return TimeUnit.NANOSECONDS.toMicros(latencyNanos);
    }


    Latency copy()
    {
        return new Latency().set(iteration, pointA, pointB, pointANanos, pointBNanos);
    }

    @Override
    public String toString()
    {
        return "Latency{" +
               "iteration=" + iteration +
               ", pointA=" + pointA +
               ", pointB=" + pointB +
               ", pointANanos=" + pointANanos +
               ", pointBNanos=" + pointBNanos +
               ", latencyNanos=" + latencyNanos +
               '}';
    }
}
