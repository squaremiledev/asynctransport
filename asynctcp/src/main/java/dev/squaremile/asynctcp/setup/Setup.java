package dev.squaremile.asynctcp.setup;

import java.io.IOException;


import dev.squaremile.asynctcp.application.ApplicationFactory;
import dev.squaremile.asynctcp.application.TransportAppLauncher;
import dev.squaremile.asynctcp.runners.NaiveRoundRobinSingleThreadRunner;

public class Setup
{
    public static void launchAsNaiveRoundRobinSingleThreadedApp(final ApplicationFactory applicationFactory) throws IOException
    {
        new NaiveRoundRobinSingleThreadRunner().run(new TransportAppLauncher().launch(applicationFactory));
    }
}
