package dev.squaremile.asynctcp.support.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;


import dev.squaremile.asynctcp.api.transport.app.OnDuty;

import static java.util.Arrays.asList;

public class ThingsOnDutyRunner
{
    private static final Consumer<OnDuty> WORK = OnDuty::work;

    private final List<OnDuty> onDuty = new ArrayList<>();

    public ThingsOnDutyRunner(final OnDuty... thingsOnDuty)
    {
        this.onDuty.addAll(asList(thingsOnDuty));
    }

    public BooleanSupplier reached(final BooleanSupplier... conditions)
    {
        return () ->
        {
            for (OnDuty duty : onDuty)
            {
                WORK.accept(duty);
            }
            boolean allConditionsMet = true;
            for (final BooleanSupplier condition : conditions)
            {
                if (!condition.getAsBoolean())
                {
                    allConditionsMet = false;
                }
            }
            return allConditionsMet;
        };
    }
}
