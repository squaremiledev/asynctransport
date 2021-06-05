package dev.squaremile.trcheck.standalone.cli;

import dev.squaremile.trcheck.standalone.TcpPingConfiguration;
import picocli.CommandLine;

import static dev.squaremile.trcheck.standalone.TcpPingConfiguration.Mode.AERON;
import static dev.squaremile.trcheck.standalone.TcpPingConfiguration.Mode.RING_BUFFERS;
import static dev.squaremile.trcheck.standalone.TcpPingConfiguration.Mode.SHARED_STACK;

@CommandLine.Command(name = "client")
public class CliBenchmarkClient
{
    @CommandLine.Option(names = {"-h", "--remote-host"}, required = true, description = "remote host")
    public String remoteHost;

    @CommandLine.Option(names = {"-p", "--remote-port"}, required = true, description = "remote port to connect to")
    public Integer remotePort;

    @CommandLine.Option(names = {"-w", "--warm-up-time"}, required = true, description = "time spent warming up [in seconds]")
    public Integer secondsWarmUp;

    @CommandLine.Option(names = {"-t", "--run-time"}, required = true, description = "time spent running [in seconds]")
    public Integer secondsRun;

    @CommandLine.Option(names = {"-s", "--send-rate"}, required = true, description = "sending rate per second")
    public Integer sendingRatePerSecond;

    @CommandLine.Option(names = {"-r", "--respond-rate"}, required = true, description = "respond rate, e.g. 32 means respond to every 32th message, 1 means respond to every message")
    public Integer respondToNth;

    @CommandLine.Option(names = {"-x", "--extra-data-length"}, required = true, description = "length of extra data in bytes included in each sent message")
    public Integer extraDataLength;

    @CommandLine.Option(names = {"-u", "--use-ring-buffers"}, description = "use ring buffers to communicate with the transport layer")
    public boolean useRingBuffers = false;

    @CommandLine.Option(names = {"-ua", "--use-aeron"}, description = "use Aeron to communicate with the transport layer")
    public boolean useAeron = false;

    public TcpPingConfiguration asConfiguration()
    {
        if (useAeron && useRingBuffers)
        {
            throw new IllegalArgumentException("--use-ring-buffers and --use-aeron can't be both set");
        }
        return new TcpPingConfiguration.Builder()
                .remoteHost(remoteHost)
                .remotePort(remotePort)
                .secondsWarmUp(secondsWarmUp)
                .secondsRun(secondsRun)
                .sendingRatePerSecond(sendingRatePerSecond)
                .respondToNth(respondToNth)
                .extraDataLength(extraDataLength)
                .mode(useAeron ? AERON : useRingBuffers ? RING_BUFFERS : SHARED_STACK)
                .create();
    }
}
