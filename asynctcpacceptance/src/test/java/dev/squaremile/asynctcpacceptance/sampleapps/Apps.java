package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.List;
import java.util.function.BooleanSupplier;


import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.testfixtures.Worker;

import static java.util.Arrays.asList;

class Apps
{
    private final List<Application> apps;

    public Apps(final Application... apps)
    {
        this.apps = asList(apps);
    }

    public void runUntil(final BooleanSupplier stopCondition)
    {
        Worker.runUntil(
                () ->
                {
                    for (Application app : apps)
                    {
                        app.work();
                    }
                    return stopCondition.getAsBoolean();
                });
    }
}
