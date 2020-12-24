package dev.squaremile.asynctcp.fixtures;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;


import dev.squaremile.asynctcp.transport.api.app.OnDuty;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

public class ThingsOnDutyRunner
{
    private final List<OnDuty> onDuty = new ArrayList<>();

    public ThingsOnDutyRunner(final OnDuty... thingsOnDuty)
    {
        this.onDuty.addAll(asList(thingsOnDuty));
    }

    public BooleanSupplier reached(final BooleanSupplier... conditions)
    {
        return () ->
        {
            onDuty.forEach(OnDuty::work);
            return stream(conditions).allMatch(BooleanSupplier::getAsBoolean);
        };
    }

    public void work()
    {
        for (OnDuty duty : onDuty)
        {
            duty.work();
        }
    }
}
