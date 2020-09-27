package dev.squaremile.asynctcp.playground;

import java.util.List;
import java.util.function.BooleanSupplier;


import dev.squaremile.asynctcp.api.app.OnDuty;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

class ThingsOnDutyRunner
{
    private final List<OnDuty> onDuty;

    public ThingsOnDutyRunner(final OnDuty... thingsOnDuty)
    {
        this.onDuty = asList(thingsOnDuty);
    }

    public BooleanSupplier reached(final BooleanSupplier... conditions)
    {
        return () ->
        {
            onDuty.forEach(OnDuty::work);
            return stream(conditions).allMatch(BooleanSupplier::getAsBoolean);
        };
    }

}
