package dev.squaremile.tcpprobe;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import org.HdrHistogram.AbstractHistogram;
import org.HdrHistogram.Histogram;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static dev.squaremile.tcpprobe.Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH;
import static dev.squaremile.tcpprobe.Metadata.DEFAULT_CORRELATION_ID_OFFSET;
import static dev.squaremile.tcpprobe.Metadata.DEFAULT_OPTIONS_OFFSET;
import static dev.squaremile.tcpprobe.Metadata.DEFAULT_SEND_TIME_OFFSET;
import static dev.squaremile.tcpprobe.Probe.probe;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofNanos;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

class ProbeTest
{
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
    private final Metadata metadata = new Metadata();

    static Stream<Function<Probe.Configuration, Probe.Configuration>> metadataOffsetsConfigurations()
    {
        return Stream.of(
                configuration -> configuration,
                configuration -> configuration.metadataOffsets(DEFAULT_OPTIONS_OFFSET, DEFAULT_SEND_TIME_OFFSET, DEFAULT_CORRELATION_ID_OFFSET),
                configuration -> configuration.metadataOffsets(0, 20, 30),
                configuration -> configuration.metadataOffsets(20, 10, 0)
        );
    }

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

        assertThat(probe.onTime(MILLISECONDS.toNanos(6_000), buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH)).isGreaterThan(0);
        probe.onMessageReceived(buffer, 0, MILLISECONDS.toNanos(6_020));
        assertThat(probe.onTime(MILLISECONDS.toNanos(7_000), buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH)).isGreaterThan(0);
        probe.onMessageReceived(buffer, 0, MILLISECONDS.toNanos(7_030));
        assertThat(probe.onTime(MILLISECONDS.toNanos(8_000), buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH)).isGreaterThan(0);
        probe.onMessageReceived(buffer, 0, MILLISECONDS.toNanos(8_070));

        assertThat(probe.hasReceivedAll()).isTrue();
        assertThat(probe.measurementsCopy().measurementsCount()).isEqualTo(totalNumberOfMessagesToSend);

        Verification verification = new Verification(probe.measurementsCopy(), ofMillis(1));
        verification.assertMeasured(AbstractHistogram::getMinValue, ofMillis(20));
        verification.assertMeasured(histogram -> (long)histogram.getMean(), ofMillis(40));
        verification.assertMeasured(AbstractHistogram::getMaxValue, ofMillis(70));
    }

    @Test
    void shouldCalculateLatencyWhenMultipleMessagesInFlight()
    {
        final Probe probe = probe("someProbe")
                .totalNumberOfMessagesToSend(3)
                .skippedResponses(0)
                .respondToEveryNthRequest(1)
                .sendingRatePerSecond(1)
                .createProbe();
        final MutableDirectBuffer inboundBuffer = new ExpandableArrayBuffer();
        final MutableDirectBuffer outboundBuffer = new ExpandableArrayBuffer();

        assertThat(probe.onTime(MILLISECONDS.toNanos(6_000), outboundBuffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH)).isGreaterThan(0);
        assertThat(metadata.wrap(outboundBuffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).correlationId()).isEqualTo(0);
        assertThat(metadata.wrap(outboundBuffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).originalTimestampNs()).isEqualTo(MILLISECONDS.toNanos(6_000));

        assertThat(probe.onTime(MILLISECONDS.toNanos(7_000), outboundBuffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH)).isGreaterThan(0);
        assertThat(metadata.wrap(outboundBuffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).correlationId()).isEqualTo(1);
        assertThat(metadata.wrap(outboundBuffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).originalTimestampNs()).isEqualTo(MILLISECONDS.toNanos(7_000));

        assertThat(probe.onTime(MILLISECONDS.toNanos(8_000), outboundBuffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH)).isGreaterThan(0);
        assertThat(metadata.wrap(outboundBuffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).correlationId()).isEqualTo(2);
        assertThat(metadata.wrap(outboundBuffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).originalTimestampNs()).isEqualTo(MILLISECONDS.toNanos(8_000));

        metadata.wrap(inboundBuffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).clear().correlationId(0).originalTimestampNs(MILLISECONDS.toNanos(6_000));
        probe.onMessageReceived(inboundBuffer, 0, MILLISECONDS.toNanos(8_100));
        metadata.wrap(inboundBuffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).clear().correlationId(1).originalTimestampNs(MILLISECONDS.toNanos(7_000));
        probe.onMessageReceived(inboundBuffer, 0, MILLISECONDS.toNanos(8_200));
        assertThat(probe.hasReceivedAll()).isFalse();
        metadata.wrap(inboundBuffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).clear().correlationId(2).originalTimestampNs(MILLISECONDS.toNanos(8_000));
        probe.onMessageReceived(inboundBuffer, 0, MILLISECONDS.toNanos(8_300));
        assertThat(probe.hasReceivedAll()).isTrue();

        assertThat(probe.measurementsCopy().measurementsCount()).isEqualTo(3);
        Verification verification = new Verification(probe.measurementsCopy(), ofMillis(5));
        verification.assertMeasured(AbstractHistogram::getMinValue, ofMillis(300));
        verification.assertMeasured(histogram -> (long)histogram.getMean(), ofMillis(1200));
        verification.assertMeasured(AbstractHistogram::getMaxValue, ofMillis(2100));
    }

    @ParameterizedTest
    @MethodSource("metadataOffsetsConfigurations")
    void shouldTakeIntoConsiderationMetadataOffsets(final Function<Probe.Configuration, Probe.Configuration> metadataOffsetsConfiguration)
    {
        Probe.Configuration configuration = metadataOffsetsConfiguration.apply(
                probe("someProbe")
                        .totalNumberOfMessagesToSend(3)
                        .skippedResponses(0)
                        .respondToEveryNthRequest(1)
                        .sendingRatePerSecond(1)
        );
        runScenario(
                configuration.createProbe(),
                new Metadata(configuration.optionsOffset(), configuration.sendTimeOffset(), configuration.correlationIdOffset())
        );
    }

    private void runScenario(final Probe probe, final Metadata supportingMetadataCodec)
    {
        final MutableDirectBuffer inboundBuffer = new ExpandableArrayBuffer();
        final MutableDirectBuffer outboundBuffer = new ExpandableArrayBuffer();
        final int enoughLength = 100;

        assertThat(probe.onTime(MILLISECONDS.toNanos(6_000), outboundBuffer, 0, enoughLength)).isGreaterThan(0);
        assertThat(supportingMetadataCodec.wrap(outboundBuffer, 0, enoughLength).correlationId()).isEqualTo(0);
        assertThat(supportingMetadataCodec.wrap(outboundBuffer, 0, enoughLength).originalTimestampNs()).isEqualTo(MILLISECONDS.toNanos(6_000));

        assertThat(probe.onTime(MILLISECONDS.toNanos(7_000), outboundBuffer, 3, enoughLength)).isGreaterThan(0);
        assertThat(supportingMetadataCodec.wrap(outboundBuffer, 3, enoughLength).correlationId()).isEqualTo(1);
        assertThat(supportingMetadataCodec.wrap(outboundBuffer, 3, enoughLength).originalTimestampNs()).isEqualTo(MILLISECONDS.toNanos(7_000));

        assertThat(probe.onTime(MILLISECONDS.toNanos(8_000), outboundBuffer, 2, enoughLength)).isGreaterThan(0);
        assertThat(supportingMetadataCodec.wrap(outboundBuffer, 2, enoughLength).correlationId()).isEqualTo(2);
        assertThat(supportingMetadataCodec.wrap(outboundBuffer, 2, enoughLength).originalTimestampNs()).isEqualTo(MILLISECONDS.toNanos(8_000));

        supportingMetadataCodec.wrap(inboundBuffer, 7, enoughLength).clear().correlationId(0).originalTimestampNs(MILLISECONDS.toNanos(6_000));
        probe.onMessageReceived(inboundBuffer, 7, MILLISECONDS.toNanos(8_100));
        supportingMetadataCodec.wrap(inboundBuffer, 0, enoughLength).clear().correlationId(1).originalTimestampNs(MILLISECONDS.toNanos(7_000));
        probe.onMessageReceived(inboundBuffer, 0, MILLISECONDS.toNanos(8_200));
        assertThat(probe.hasReceivedAll()).isFalse();
        supportingMetadataCodec.wrap(inboundBuffer, 9, enoughLength).clear().correlationId(2).originalTimestampNs(MILLISECONDS.toNanos(8_000));
        probe.onMessageReceived(inboundBuffer, 9, MILLISECONDS.toNanos(8_300));
        assertThat(probe.hasReceivedAll()).isTrue();

        assertThat(probe.measurementsCopy().measurementsCount()).isEqualTo(3);
        Verification verification = new Verification(probe.measurementsCopy(), ofMillis(5));
        verification.assertMeasured(AbstractHistogram::getMinValue, ofMillis(300));
        verification.assertMeasured(histogram -> (long)histogram.getMean(), ofMillis(1200));
        verification.assertMeasured(AbstractHistogram::getMaxValue, ofMillis(2100));
    }

    @Test
    void shouldTakeIntoAccountCoordinatedOmission()
    {
        final Probe probe = probe("someProbe")
                .totalNumberOfMessagesToSend(3)
                .skippedResponses(0)
                .respondToEveryNthRequest(1)
                .sendingRatePerSecond((int)TimeUnit.SECONDS.toMicros(1))
                .createProbe();

        assertThat(probe.onTime(MICROSECONDS.toNanos(300), buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH)).isGreaterThan(0);
        probe.onMessageReceived(buffer, 0, MICROSECONDS.toNanos(302));
        assertThat(probe.onTime(MICROSECONDS.toNanos(305), buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH)).isGreaterThan(0); // should have sent at 301us
        probe.onMessageReceived(buffer, 0, MICROSECONDS.toNanos(307)); // round trip 2us, but 6us with coordinated omission
        assertThat(probe.onTime(MICROSECONDS.toNanos(310), buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH)).isGreaterThan(0); // should have sent at 302us
        probe.onMessageReceived(buffer, 0, MICROSECONDS.toNanos(312)); // round trip 2us, but 10us with coordinated omission

        Verification verification = new Verification(probe.measurementsCopy(), ofNanos(100));
        verification.assertMeasured(AbstractHistogram::getMinValue, ofNanos(2_000));
        verification.assertMeasured(histogram -> (long)histogram.getMean(), ofNanos(6_000));
        verification.assertMeasured(AbstractHistogram::getMaxValue, ofNanos(10_000));
    }

    @Test
    void shouldTakeIntoAccountCoordinatedOmissionWhenMultipleMessagesInFlight()
    {
        final Probe probe = probe("someProbe")
                .totalNumberOfMessagesToSend(3)
                .skippedResponses(0)
                .respondToEveryNthRequest(1)
                .sendingRatePerSecond((int)TimeUnit.SECONDS.toMicros(1))
                .createProbe();

        assertThat(probe.onTime(MICROSECONDS.toNanos(300), buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH)).isGreaterThan(0);
        assertThat(metadata.wrap(buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).correlationId()).isEqualTo(0);
        assertThat(metadata.wrap(buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).originalTimestampNs()).isEqualTo(MICROSECONDS.toNanos(300));


        assertThat(probe.onTime(MICROSECONDS.toNanos(305), buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH)).isGreaterThan(0); // should have sent at 301us
        assertThat(metadata.wrap(buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).correlationId()).isEqualTo(1);
        assertThat(metadata.wrap(buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).originalTimestampNs()).isEqualTo(MICROSECONDS.toNanos(301));


        assertThat(probe.onTime(MICROSECONDS.toNanos(310), buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH)).isGreaterThan(0); // should have sent at 302us
        assertThat(metadata.wrap(buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).correlationId()).isEqualTo(2);
        assertThat(metadata.wrap(buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).originalTimestampNs()).isEqualTo(MICROSECONDS.toNanos(302));

        metadata.wrap(buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).clear().correlationId(0).originalTimestampNs(MICROSECONDS.toNanos(300));
        probe.onMessageReceived(buffer, 0, MICROSECONDS.toNanos(352));
        metadata.wrap(buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).clear().correlationId(1).originalTimestampNs(MICROSECONDS.toNanos(301));
        probe.onMessageReceived(buffer, 0, MICROSECONDS.toNanos(357));
        metadata.wrap(buffer, 0, ALL_METADATA_FIELDS_TOTAL_LENGTH).clear().correlationId(2).originalTimestampNs(MICROSECONDS.toNanos(302));
        probe.onMessageReceived(buffer, 0, MICROSECONDS.toNanos(362));

        Verification verification = new Verification(probe.measurementsCopy(), ofNanos(100));
        verification.assertMeasured(AbstractHistogram::getMinValue, ofNanos(52_000));
        verification.assertMeasured(histogram -> (long)histogram.getMean(), ofNanos(56_000));
        verification.assertMeasured(AbstractHistogram::getMaxValue, ofNanos(60_000));
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
            long expectedUs = TimeUnit.SECONDS.toMicros(duration.getSeconds()) + TimeUnit.NANOSECONDS.toMicros(duration.getNano());
            long toleranceUs = TimeUnit.SECONDS.toMicros(tolerance.getSeconds()) + TimeUnit.NANOSECONDS.toMicros(tolerance.getNano());
            assertThat(actualUs).isCloseTo(expectedUs, Offset.offset(toleranceUs));
        }
    }
}