package dev.squaremile.trcheck.standalone.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "driver")
public class CliBenchmarkDriver
{
    @CommandLine.Option(names = {"-d", "--driver-directory"}, description = "driver directory to use or randomized if empty")
    public String directory = "";
}
