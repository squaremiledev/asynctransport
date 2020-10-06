package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.List;
import java.util.function.BooleanSupplier;


import dev.squaremile.asynctcp.transport.setup.TransportApplication;
import dev.squaremile.asynctcp.transport.testfixtures.Worker;

import static java.util.Arrays.asList;

class Apps
{
    private final List<TransportApplication> apps;

    public Apps(final TransportApplication... apps)
    {
        this.apps = asList(apps);
    }

    public void runUntil(final BooleanSupplier stopCondition)
    {
        Worker.runUntil(
                () ->
                {
                    for (TransportApplication app : apps)
                    {
                        app.work();
                    }
                    return stopCondition.getAsBoolean();
                });
    }
}
