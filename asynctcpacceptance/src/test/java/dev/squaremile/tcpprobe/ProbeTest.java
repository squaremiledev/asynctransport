package dev.squaremile.tcpprobe;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.HdrHistogram.AbstractHistogram;
import org.HdrHistogram.Histogram;
import org.agrona.ExpandableArrayBuffer;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static dev.squaremile.tcpprobe.Probe.probe;
import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

class ProbeTest
{
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();

    @Test
    void shouldPreventFromObtainingReportWithNoData()
    {
        final Probe probe = probe("someProbe")
                .totalNumberOfMessagesToSend(1)
                .skippedResponses(0)
                .respondToEveryNthRequest(1)
                .sendingRatePerSecond(1)
                .createProbe();

        assertThat(probe.hasReceivedAll()).isFalse();
        assertThrows(IllegalStateException.class, probe::measurementsCopy);
    }

    @Test
    void shouldPreventFromObtainingReportWithInsufficientData()
    {
        int totalNumberOfMessagesToSend = 3;
        final Probe probe = probe("someProbe")
                .totalNumberOfMessagesToSend(totalNumberOfMessagesToSend)
                .skippedResponses(0)
                .respondToEveryNthRequest(1)
                .sendingRatePerSecond(1)
                .createProbe();

        assertThat(probe.onTime(MILLISECONDS.toNanos(6_000), buffer, 0)).isGreaterThan(0);
        probe.onMessageReceived(buffer, 0, MILLISECONDS.toNanos(6_020));
        assertThat(probe.onTime(MILLISECONDS.toNanos(7_000), buffer, 0)).isGreaterThan(0);
        probe.onMessageReceived(buffer, 0, MILLISECONDS.toNanos(7_030));
        assertThat(probe.onTime(MILLISECONDS.toNanos(8_000), buffer, 0)).isGreaterThan(0);
        probe.onMessageReceived(buffer, 0, MILLISECONDS.toNanos(8_070));

        assertThat(probe.hasReceivedAll()).isTrue();
        assertThat(probe.measurementsCopy().measurementsCount()).isEqualTo(totalNumberOfMessagesToSend);

        Verification verification = new Verification(probe.measurementsCopy(), ofMillis(1));
        verification.assertMeasured(AbstractHistogram::getMinValue, ofMillis(20));
        verification.assertMeasured(histogram -> (long)histogram.getMean(), ofMillis(40));
        verification.assertMeasured(AbstractHistogram::getMaxValue, ofMillis(70));
    }

    private static class Verification
    {
        private final Measurements measurements;
        private final Duration tolerance;

        public Verification(final Measurements measurements, final Duration tolerance)
        {
            this.measurements = measurements;
            this.tolerance = tolerance;
        }

        public void assertMeasured(final Function<Histogram, Long> value, final Duration duration)
        {
            long actualUs = value.apply(measurements.histogramUs());
            long expectedUs = TimeUnit.NANOSECONDS.toMicros(duration.getNano());
            long toleranceUs = TimeUnit.NANOSECONDS.toMicros(tolerance.getNano());
            assertThat(actualUs).isCloseTo(expectedUs, Offset.offset(toleranceUs));
        }
    }
}