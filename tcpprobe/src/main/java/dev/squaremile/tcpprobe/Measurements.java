package dev.squaremile.tcpprobe;

import java.util.concurrent.TimeUnit;

import org.HdrHistogram.Histogram;


import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class Measurements
{
    private final Histogram histogram;
    private final String description;
    private final int measureFromNthReceived;
    private long firstMeasuredMessageSentNs;
    private long lastMeasuredMessageReceivedNs;

    public Measurements(final String description, final int measureFromNthReceived)
    {
        this.description = description;
        this.measureFromNthReceived = measureFromNthReceived;
        this.histogram = new Histogram(TimeUnit.SECONDS.toNanos(10), 3);
    }

    private Measurements(final Measurements copySrc)
    {
        this.histogram = copySrc.histogram.copy();
        this.description = copySrc.description;
        this.measureFromNthReceived = copySrc.measureFromNthReceived;
        this.firstMeasuredMessageSentNs = copySrc.firstMeasuredMessageSentNs;
        this.lastMeasuredMessageReceivedNs = copySrc.lastMeasuredMessageReceivedNs;
    }

    private static int half(final double value)
    {
        return (int)Math.ceil(value / 2.0);
    }

    public void onMessageReceived(final long messagesSentCount, final long messagesReceivedCount, final long messageSentTimeNs, final long messageReceivedTimeNs)
    {
        if (messagesReceivedCount < measureFromNthReceived)
        {
            return;
        }
        if (messagesReceivedCount == measureFromNthReceived)
        {
            firstMeasuredMessageSentNs = messageSentTimeNs;
        }
        lastMeasuredMessageReceivedNs = messageReceivedTimeNs;
        final long latencyUs = NANOSECONDS.toMicros(messageReceivedTimeNs - messageSentTimeNs);
        histogram.recordValue(latencyUs);
    }

    public void printResults()
    {
        System.out.println();
        System.out.println("Scenario: " + description);
        System.out.println("Results:");
        System.out.println("---------------------------------------------------------");
        System.out.printf("latency (microseconds) |     ~ one way |     round trip |%n");
        System.out.printf("mean                   |     %9d |      %9d |%n", half(histogram.getMean()), (int)Math.ceil(histogram.getMean()));
        System.out.printf("99th percentile        |     %9d |      %9d |%n", half(histogram.getValueAtPercentile(99)), histogram.getValueAtPercentile(99));
        System.out.printf("99.9th percentile      |     %9d |      %9d |%n", half(histogram.getValueAtPercentile(99.9)), histogram.getValueAtPercentile(99.9));
        System.out.printf("99.99th percentile     |     %9d |      %9d |%n", half(histogram.getValueAtPercentile(99.99)), histogram.getValueAtPercentile(99.99));
        System.out.printf("99.999th percentile    |     %9d |      %9d |%n", half(histogram.getValueAtPercentile(99.999)), histogram.getValueAtPercentile(99.999));
        System.out.printf("worst                  |     %9d |      %9d |%n", half(histogram.getMaxValue()), histogram.getMaxValue());
        System.out.println();
        System.out.println("Based on " + histogram.getTotalCount() + " measurements.");
        System.out.println(
                "It took " +
                NANOSECONDS.toMillis(lastMeasuredMessageReceivedNs - firstMeasuredMessageSentNs) +
                " ms between the first measured message sent and the last received"
        );
        System.out.println();

    }

    public long measurementsCount()
    {
        return histogram.getTotalCount();
    }

    public Histogram histogramUs()
    {
        return histogram;
    }

    public Measurements copy()
    {
        return new Measurements(this);
    }
}
