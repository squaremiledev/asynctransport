package dev.squaremile.trcheck.standalone;

import dev.squaremile.trcheck.standalone.cli.CliBenchmarkClient;
import dev.squaremile.trcheck.standalone.cli.CliBenchmarkDriver;
import dev.squaremile.trcheck.standalone.cli.CliBenchmarkServer;
import dev.squaremile.trcheck.standalone.cli.CliParser;

import static java.util.Arrays.stream;

public class Benchmark
{
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            usageAndExit();
        }
        if ("server".equals(args[0]))
        {
            CliParser.Parsed<CliBenchmarkServer> parsed = CliParser.parseArgs(new CliBenchmarkServer(), System.err, System.out, stream(args).skip(1).toArray(String[]::new));
            parsed.exitCode.ifPresent(System::exit);
            EchoApplication.start(parsed.cli.port, () -> System.out.println("Listening on port " + parsed.cli.port), () ->
            {
            });
            System.exit(0);
        }
        else if ("driver".equals(args[0]))
        {
            CliParser.Parsed<CliBenchmarkDriver> parsed = CliParser.parseArgs(new CliBenchmarkDriver(), System.err, System.out, stream(args).skip(1).toArray(String[]::new));
            parsed.exitCode.ifPresent(System::exit);
            DriverApplication.start(parsed.cli.directory);
        }
        else if ("client".equals(args[0]))
        {
            CliParser.Parsed<CliBenchmarkClient> parsed = CliParser.parseArgs(new CliBenchmarkClient(), System.err, System.out, stream(args).skip(1).toArray(String[]::new));
            parsed.exitCode.ifPresent(System::exit);
            TcpPingConfiguration configuration = parsed.cli.asConfiguration();
            System.out.println(configuration);
            SourcingConnectionApplication.runPing(configuration).printResults();
            System.exit(0);
        }
        else
        {
            usageAndExit();
        }
    }

    private static void usageAndExit()
    {
        System.out.println("Usage: trcheck benchmark server|client|driver options...");
        System.exit(1);
    }
}
