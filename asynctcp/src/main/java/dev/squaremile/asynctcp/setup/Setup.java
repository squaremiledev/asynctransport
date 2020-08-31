package dev.squaremile.asynctcp.setup;

import java.util.stream.Collectors;


import dev.squaremile.asynctcp.application.ApplicationFactory;
import dev.squaremile.asynctcp.application.TransportAppLauncher;
import dev.squaremile.asynctcp.runners.NaiveRoundRobinSingleThreadRunner;

import static java.util.Arrays.stream;

public class Setup
{
    public static void launchAsNaiveRoundRobinSingleThreadedApp(final ApplicationFactory... applicationFactories)
    {
        TransportAppLauncher transportAppLauncher = new TransportAppLauncher();
        new NaiveRoundRobinSingleThreadRunner().run(stream(applicationFactories).map(transportAppLauncher::launch).collect(Collectors.toList()));
    }
}
