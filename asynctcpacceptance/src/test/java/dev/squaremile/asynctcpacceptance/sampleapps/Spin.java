package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;


import dev.squaremile.asynctcp.setup.TransportApplication;
import dev.squaremile.asynctcp.api.app.CommandFailed;
import dev.squaremile.asynctcp.testfixtures.TransportEventsSpy;
import dev.squaremile.asynctcp.testfixtures.app.WhiteboxApplication;

import static dev.squaremile.asynctcp.testfixtures.Worker.runUntil;

class Spin
{
    private final WhiteboxApplication<TransportEventsSpy> whiteboxApplication;
    private final List<TransportApplication> applications;

    Spin(final WhiteboxApplication<TransportEventsSpy> whiteboxApplication, final TransportApplication... applications)
    {
        this.whiteboxApplication = whiteboxApplication;
        this.applications = Arrays.asList(applications);
    }

    Spin(final TransportApplication... applications)
    {
        this(null, applications);
    }

    void spinUntil(final BooleanSupplier endCondition)
    {
        spinUntil(false, endCondition);
    }

    void spinUntilAllowingFailures(final BooleanSupplier endCondition)
    {
        spinUntil(true, endCondition);
    }

    void spinUntil(final boolean allowFailures, final BooleanSupplier endCondition)
    {
        runUntil(() ->
                 {
                     for (final TransportApplication application : applications)
                     {
                         application.work();
                     }
                     if (!allowFailures && whiteboxApplication != null)
                     {
                         throwOnFailures();
                     }
                     return endCondition.getAsBoolean();
                 });
    }

    private void throwOnFailures()
    {
        List<CommandFailed> failures = whiteboxApplication.events().all(CommandFailed.class);
        if (!failures.isEmpty())
        {
            throw new IllegalStateException("Failure occurred: " + failures);
        }
    }
}
