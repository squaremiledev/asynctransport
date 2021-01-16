package dev.squaremile.trcheck.probe.latencies;

import java.util.function.LongSupplier;

public class Latencies
{
    private final Measurement[] measurements;
    private final Latency currentMaxLatency = new Latency();
    private final LongSupplier timeSource;
    private long currentNanoTimeMark = 0;
    private int nextMeasurementIndex = 0;
    private long iteration = 0;
    private boolean iterationActive = false;
    private boolean warmUp;

    public Latencies(final int maxNumberOfUniqueMeasurementPoints)
    {
        this(maxNumberOfUniqueMeasurementPoints, null);
    }

    public Latencies(final int maxNumberOfUniqueMeasurementPoints, final LongSupplier timeSource)
    {
        this.timeSource = timeSource;
        measurements = new Measurement[maxNumberOfUniqueMeasurementPoints];
        for (int i = 0; i < measurements.length; i++)
        {
            measurements[i] = new Measurement();
        }
    }

    public Latencies iterationStart()
    {
        return iterationStart(false);
    }

    public Latencies iterationStart(final boolean warmUp)
    {
        this.warmUp = warmUp;
        verifyIterationNotActive();
        iterationActive = true;
        iteration++;
        return this;
    }

    public Latencies measure(final int measurementPoint)
    {
        return measure(measurementPoint, timeSource.getAsLong());
    }

    public Latencies measure(final int measurementPoint, final long nanoTime)
    {
        verifyNotGoingBackInTime(nanoTime);
        verifyIterationActive();
        measurements[nextMeasurementIndex++].set(measurementPoint, nanoTime);
        return this;
    }

    public Latencies iterationDone()
    {
        verifyIterationActive();
        verifyCanFinishIteration();
        iterationActive = false;
        nextMeasurementIndex = 0;
        if (!warmUp)
        {
            Measurement previousMeasurement = null;
            for (final Measurement measurement : measurements)
            {
                if (previousMeasurement != null)
                {
                    long latencyNanos = measurement.nanoTime - previousMeasurement.nanoTime;
                    if (currentMaxLatency.latencyNanos() < latencyNanos)
                    {
                        currentMaxLatency.set(iteration, previousMeasurement.point, measurement.point, previousMeasurement.nanoTime, measurement.nanoTime);
                    }
                }
                previousMeasurement = measurement;
            }
        }
        return this;
    }

    private void verifyIterationActive()
    {
        if (!iterationActive)
        {
            throw new IllegalStateException("Iteration not active");
        }
    }

    private void verifyNotGoingBackInTime(final long currentNano)
    {
        if (currentNanoTimeMark > currentNano)
        {
            throw new IllegalStateException("Going back in time not allowed: " + currentNanoTimeMark + " -> " + currentNano);
        }
        currentNanoTimeMark = currentNano;
    }

    private void verifyCanFinishIteration()
    {
        if (nextMeasurementIndex < 2)
        {
            throw new IllegalStateException("Not enough data points");
        }
    }

    private void verifyIterationNotActive()
    {
        if (iterationActive)
        {
            throw new IllegalStateException("Iteration active");
        }
    }

    public Latency maxLatency()
    {
        return currentMaxLatency.copy();
    }

}
