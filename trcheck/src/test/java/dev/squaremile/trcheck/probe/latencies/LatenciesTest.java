package dev.squaremile.trcheck.probe.latencies;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import org.agrona.collections.MutableLong;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LatenciesTest
{
    @Test
    void shouldMeasureUsingProvidedTimeSource()
    {
        final MutableLong timeSource = new MutableLong(100);
        Latencies latencies = new Latencies(10, timeSource::incrementAndGet)
                .iterationStart()
                .measure(1).measure(3).measure(9).measure(10)
                .iterationDone();

        assertThat(latencies.maxLatency()).usingRecursiveComparison().isEqualTo(new Latency().set(1, 1, 3, 101, 102));
        assertThat(latencies.maxLatency().latencyNanos()).isEqualTo(1);
    }

    @Test
    void shouldKeepTrackOfTheHighestLatencyBetweenTwoPoints()
    {
        Latencies latencies = new Latencies(10)
                .iterationStart()
                .measure(1, 50).measure(2, 55).measure(3, 76).measure(4, 80)
                .iterationDone();

        assertThat(latencies.maxLatency()).usingRecursiveComparison().isEqualTo(new Latency().set(1, 2, 3, 55, 76));
        assertThat(latencies.maxLatency().latencyNanos()).isEqualTo(21);
    }

    @Test
    void shouldFindMaxLatencyAcrossMultipleIterations()
    {
        Latencies latencies = new Latencies(4)
                .iterationStart().measure(1, 50).measure(2, 53).measure(3, 60).iterationDone()
                .iterationStart().measure(3, 60).measure(4, 69).measure(5, 70).measure(9, 71).iterationDone()
                .iterationStart().measure(1, 80).measure(7, 82).measure(14, 90).iterationDone();

        assertThat(latencies.maxLatency()).usingRecursiveComparison().isEqualTo(new Latency().set(2, 3, 4, 60, 69));
        assertThat(latencies.maxLatency().latencyNanos()).isEqualTo(9);
    }

    @Test
    void shouldNotTakeIntoAccountWarUpIterations()
    {
        Latencies latencies = new Latencies(4)
                .iterationStart().measure(1, 50).measure(2, 55).measure(3, 60).iterationDone()
                .iterationStart(true).measure(3, 600).measure(4, 700).measure(5, 800).measure(9, 850).iterationDone()
                .iterationStart().measure(1, 1050).measure(2, 1053).measure(3, 1060).iterationDone()
                .iterationStart().measure(1, 1080).measure(7, 1082).measure(14, 1083).iterationDone();

        assertThat(latencies.maxLatency()).usingRecursiveComparison().isEqualTo(new Latency().set(3, 2, 3, 1053, 1060));
        assertThat(latencies.maxLatency().latencyNanos()).isEqualTo(7);
    }

    @Test
    void shouldAllowMeasurementsInAnyOrder()
    {
        Latencies latencies = new Latencies(10)
                .iterationStart()
                .measure(1, 1).measure(9, 2).measure(3, 4).measure(4, 5)
                .iterationDone();

        assertThat(latencies.maxLatency()).usingRecursiveComparison().isEqualTo(new Latency().set(1, 9, 3, 2, 4));
        assertThat(latencies.maxLatency().latencyNanos()).isEqualTo(2);
    }

    @Test
    void shouldNotAllowGoingBackInTime()
    {
        Latencies latencies = new Latencies(10).iterationStart().measure(1, 10);
        assertThrows(IllegalStateException.class, () -> latencies.measure(2, 9));
    }

    @Test
    void shouldBeUnableToCalculateLatencyFromOnlyOneDataPoint()
    {
        Latencies latencies = new Latencies(10).iterationStart().measure(1, 50);

        assertThrows(IllegalStateException.class, latencies::iterationDone);
    }

    @Test
    void shouldOnlyMeasureAfterIterationStarted()
    {
        Latencies latencies1 = new Latencies(10);
        assertThrows(IllegalStateException.class, () -> latencies1.measure(1, 2));
    }

    @Test
    void shouldOnlyMeasureBeforeIterationDone()
    {
        Latencies latencies = new Latencies(10).iterationStart().measure(1, 10).measure(2, 20).iterationDone();
        assertThrows(IllegalStateException.class, () -> latencies.measure(1, 2));
    }

    @Test
    void shouldNotAllowToStartOrStopIterationMoreThanOnce()
    {
        assertThrows(IllegalStateException.class, () -> new Latencies(10).iterationDone());
        assertThrows(IllegalStateException.class, () -> new Latencies(10).iterationStart().iterationStart());
        assertThrows(IllegalStateException.class, () -> new Latencies(10).iterationStart().iterationDone().iterationDone());
    }

    @Test
    void shouldHaveNegligibleOverheadWhenUsed()
    {
        final MutableLong timeSupplier = new MutableLong(0);
        final Latencies latencies = new Latencies(5, timeSupplier::get);
        int measurements = 0;
        long start = System.nanoTime();
        for (int i = 0; i < 5_000_000; i++)
        {
            latencies.iterationStart();
            for (int j = 0; j < 5; j++)
            {
                timeSupplier.set(i * 10 + j + measurements++);
                latencies.measure(j);
            }
            latencies.iterationDone();
        }
        long elapsedNs = System.nanoTime() - start;
        long overheadNs = elapsedNs / measurements;
        assertThat(overheadNs).isLessThan(100); // sth around 5ns, but better not fail the build as such speed is unnecessary
        System.out.println("overheadNs = " + overheadNs);
    }

    @Test
    @Disabled
        // needs to run longer to be useful and System.nanoTime has its overhead as well
    void shouldBeAbleToUseSystemTime()
    {
        Map<String, Long> withoutMeasurements = benchmark(false, System::nanoTime);
        Map<String, Long> withMeasurements = benchmark(true, System::nanoTime);

        System.out.println("withoutMeasurements = " + withoutMeasurements);
        System.out.println("withMeasurements = " + withMeasurements);
        assertThat(TimeUnit.NANOSECONDS.toMicros(withoutMeasurements.get("maxNs"))).isLessThan(300);
        assertThat(TimeUnit.NANOSECONDS.toMicros(withMeasurements.get("maxNs"))).isLessThan(300);
        long avgOverheadMicroseconds = TimeUnit.NANOSECONDS.toMicros(withMeasurements.get("avgNs") - withoutMeasurements.get("avgNs"));
        assertThat(avgOverheadMicroseconds).isLessThanOrEqualTo(1);
    }

    private static Map<String, Long> benchmark(final boolean measureLatencies, final LongSupplier timeNsSupplier)
    {
        Latencies latencies = new Latencies(2, timeNsSupplier);
        long startTime = -1;
        long counter = -1;
        long max = -1;
        for (int k = 0; k < 100; k++)
        {
            boolean warmUp = k < 40;
            for (int i = 0; i < 100_000; i++)
            {
                for (int j = 0; j < 5; j++)
                {
                    if (!warmUp)
                    {
                        if (startTime == -1)
                        {
                            startTime = System.nanoTime();
                        }
                        counter++;
                    }
                    long before = timeNsSupplier.getAsLong();
                    long after = timeNsSupplier.getAsLong();
                    if (measureLatencies)
                    {
                        latencies.iterationStart(warmUp).measure(1, before).measure(2, after).iterationDone();
                    }
                    if (!warmUp)
                    {
                        if (max < after - before)
                        {
                            max = after - before;
                        }
                    }
                }
            }
        }
        Map<String, Long> result = new HashMap<>();
        result.put("maxNs", max);
        result.put("avgNs", (System.nanoTime() - startTime) / counter);
        return result;
    }
}