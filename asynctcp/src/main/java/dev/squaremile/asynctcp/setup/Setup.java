package dev.squaremile.asynctcp.setup;

import java.util.stream.Collectors;


import dev.squaremile.asynctcp.api.app.ApplicationFactory;

import static java.util.Arrays.stream;

public class Setup
{
    public static void launchAsNaiveRoundRobinSingleThreadedApp(final ApplicationFactory... applicationFactories)
    {
        TransportAppLauncher transportAppLauncher = new TransportAppLauncher();
        new NaiveRoundRobinSingleThreadRunner().run(stream(applicationFactories).map(applicationFactory -> transportAppLauncher.launch(applicationFactory, "")).collect(Collectors.toList()));
    }
}
