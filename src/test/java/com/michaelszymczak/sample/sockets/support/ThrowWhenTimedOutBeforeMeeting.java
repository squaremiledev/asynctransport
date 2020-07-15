package com.michaelszymczak.sample.sockets.support;

import java.util.function.BooleanSupplier;

public class ThrowWhenTimedOutBeforeMeeting implements BooleanSupplier
{
    private static final int DEFAULT_TIMEOUT_MS = 1_000;

    private final BooleanSupplier condition;

    public ThrowWhenTimedOutBeforeMeeting(final BooleanSupplier condition)
    {
        final long startTime = System.currentTimeMillis();
        this.condition = () ->
        {
            final boolean conditionMet = condition.getAsBoolean();
            if (conditionMet)
            {
                return true;
            }
            final boolean hasTimedOut = startTime + DEFAULT_TIMEOUT_MS <= System.currentTimeMillis();
            if (!hasTimedOut)
            {
                return false;
            }
            throw new RuntimeException("Not completed within " + DEFAULT_TIMEOUT_MS + "ms");
        };
    }

    public static BooleanSupplier timeoutOr(final BooleanSupplier condition)
    {
        return new ThrowWhenTimedOutBeforeMeeting(condition);
    }

    @Override
    public boolean getAsBoolean()
    {
        return condition.getAsBoolean();
    }
}
