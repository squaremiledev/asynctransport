package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;


import dev.squaremile.asynctcp.application.TransportApplication;
import dev.squaremile.asynctcp.domain.api.events.CommandFailed;
import dev.squaremile.asynctcp.testfitures.app.WhiteboxApplication;

import static dev.squaremile.asynctcp.testfitures.Worker.runUntil;

class Spin
{
    private final WhiteboxApplication whiteboxApplication;
    private final List<TransportApplication> applications;

    Spin(final WhiteboxApplication whiteboxApplication, final TransportApplication... applications)
    {
        this.whiteboxApplication = whiteboxApplication;
        this.applications = Arrays.asList(applications);
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
                     if (!allowFailures)
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
