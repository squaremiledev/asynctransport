package dev.squaremile.trcheck.standalone.cli;

import java.io.PrintStream;
import java.util.OptionalInt;


import picocli.CommandLine;

public class CliParser
{
    public static <T> Parsed<T> parseArgs(
            final T cli,
            final PrintStream errStream,
            final PrintStream outStream,
            final String... args
    )
    {
        final CommandLine commandLine = new CommandLine(cli);
        try
        {
            CommandLine.ParseResult parseResult = commandLine.parseArgs(args);
            if (!parseResult.errors().isEmpty())
            {
                errStream.println(parseResult.errors());
                commandLine.usage(errStream);
                return new Parsed<>(cli, OptionalInt.of(1));
            }
            if (parseResult.isUsageHelpRequested())
            {
                commandLine.usage(outStream);
                return new Parsed<>(cli, OptionalInt.of(0));
            }
            return new Parsed<>(cli, OptionalInt.empty());
        }
        catch (Exception e)
        {
            errStream.println(e.getMessage());
            commandLine.usage(errStream);
            return new Parsed<>(cli, OptionalInt.of(1));
        }
    }

    public static class Parsed<T>
    {
        public final T cli;
        public final OptionalInt exitCode;

        public Parsed(final T cli, final OptionalInt exitCode)
        {
            this.cli = cli;
            this.exitCode = exitCode;
        }
    }
}
