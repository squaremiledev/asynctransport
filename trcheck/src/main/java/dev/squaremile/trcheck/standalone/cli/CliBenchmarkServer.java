package dev.squaremile.trcheck.standalone.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "server")
public class CliBenchmarkServer
{
    @CommandLine.Option(names = {"-p", "--port"}, required = true, description = "port to listen on")
    public Integer port;
}
