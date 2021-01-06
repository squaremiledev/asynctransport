package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.List;
import java.util.function.BooleanSupplier;


import dev.squaremile.asynctcp.api.transport.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.fixtures.transport.Worker;

import static java.util.Arrays.asList;

class Apps
{
    private final List<ApplicationOnDuty> apps;

    public Apps(final ApplicationOnDuty... apps)
    {
        this.apps = asList(apps);
    }

    public void runUntil(final BooleanSupplier stopCondition)
    {
        Worker.runUntil(
                () ->
                {
                    for (ApplicationOnDuty app : apps)
                    {
                        app.work();
                    }
                    return stopCondition.getAsBoolean();
                });
    }
}
