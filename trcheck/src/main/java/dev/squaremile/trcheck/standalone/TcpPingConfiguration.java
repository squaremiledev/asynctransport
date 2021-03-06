package dev.squaremile.trcheck.standalone;

import dev.squaremile.trcheck.probe.Probe;

import static dev.squaremile.trcheck.probe.Probe.probe;
import static java.util.Objects.requireNonNull;

public class TcpPingConfiguration
{
    private final int sendingRatePerSecond;
    private final int respondToNth;
    private final int secondsRun;
    private final int secondsWarmUp;
    private final String remoteHost;
    private final int remotePort;
    private final int extraDataLength;
    private final String driverDirectory;
    private final Mode mode;

    private TcpPingConfiguration(
            final int sendingRatePerSecond,
            final int respondToNth,
            final int secondsRun,
            final int secondsWarmUp,
            final int extraDataLength,
            final String remoteHost,
            final int remotePort,
            final Mode mode,
            final String driverDirectory
    )
    {
        this.extraDataLength = extraDataLength;
        this.driverDirectory = driverDirectory;
        if (sendingRatePerSecond <= 0 || respondToNth <= 0 || secondsRun <= 0 || secondsWarmUp <= 0 || extraDataLength < 0 || remoteHost == null || remoteHost.trim().isEmpty() || remotePort <= 0)
        {
            throw new IllegalArgumentException();
        }

        this.sendingRatePerSecond = sendingRatePerSecond;
        this.respondToNth = respondToNth;
        this.secondsRun = secondsRun;
        this.secondsWarmUp = secondsWarmUp;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.mode = mode;
    }

    public Probe.Configuration probeConfig()
    {
        return probe("messageSource")
                .totalNumberOfMessagesToSend(sendingRatePerSecond() * (secondsWarmUp() + secondsRun()))
                .skippedResponses((sendingRatePerSecond() * secondsWarmUp()) / respondToNth())
                .respondToEveryNthRequest(respondToNth())
                .sendingRatePerSecond(sendingRatePerSecond());
    }

    public String driverDirectory()
    {
        return driverDirectory;
    }

    public int sendingRatePerSecond()
    {
        return sendingRatePerSecond;
    }

    public int respondToNth()
    {
        return respondToNth;
    }

    public int secondsRun()
    {
        return secondsRun;
    }

    public int secondsWarmUp()
    {
        return secondsWarmUp;
    }

    public int extraDataLength()
    {
        return extraDataLength;
    }

    public String remoteHost()
    {
        return remoteHost;
    }

    public int remotePort()
    {
        return remotePort;
    }

    public Mode mode()
    {
        return mode;
    }

    @Override
    public String toString()
    {
        return "TcpPingConfiguration{" +
               "sendingRatePerSecond=" + sendingRatePerSecond +
               ", respondToNth=" + respondToNth +
               ", secondsRun=" + secondsRun +
               ", secondsWarmUp=" + secondsWarmUp +
               ", remoteHost='" + remoteHost + '\'' +
               ", remotePort=" + remotePort +
               ", extraDataLength=" + extraDataLength +
               ", driverDirectory='" + driverDirectory + '\'' +
               ", mode=" + mode +
               '}';
    }

    public enum Mode
    {
        SHARED_STACK,
        RING_BUFFERS,
        AERON
    }

    public static class Builder
    {
        private int sendingRatePerSecond;
        private int respondToNth;
        private int secondsRun;
        private int secondsWarmUp;
        private String remoteHost;
        private int remotePort;
        private int extraDataLength;
        private Mode mode = Mode.SHARED_STACK;
        private String driverDirectory = "";

        public Builder sendingRatePerSecond(final int sendingRatePerSecond)
        {
            this.sendingRatePerSecond = sendingRatePerSecond;
            return this;
        }

        public Builder respondToNth(final int respondToNth)
        {
            this.respondToNth = respondToNth;
            return this;
        }

        public Builder secondsRun(final int secondsRun)
        {
            this.secondsRun = secondsRun;
            return this;
        }

        public Builder secondsWarmUp(final int secondsWarmUp)
        {
            this.secondsWarmUp = secondsWarmUp;
            return this;
        }

        public Builder extraDataLength(final int extraDataLength)
        {
            this.extraDataLength = extraDataLength;
            return this;
        }

        public Builder remoteHost(final String remoteHost)
        {
            this.remoteHost = requireNonNull(remoteHost);
            return this;
        }

        public Builder remotePort(final int remotePort)
        {
            this.remotePort = remotePort;
            return this;
        }

        public Builder mode(Mode mode)
        {
            this.mode = requireNonNull(mode);
            return this;
        }

        public Builder directory(final String driverDirectory)
        {
            this.driverDirectory = requireNonNull(driverDirectory);
            return this;
        }

        public TcpPingConfiguration create()
        {
            return new TcpPingConfiguration(sendingRatePerSecond, respondToNth, secondsRun, secondsWarmUp, extraDataLength, remoteHost, remotePort, mode, driverDirectory);
        }
    }
}
