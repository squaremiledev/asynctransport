package dev.squaremile.asynctcp.fixtures.transport;

import java.util.function.BooleanSupplier;

public class ThrowWhenTimedOutBeforeMeeting implements BooleanSupplier
{
    public static final int DEFAULT_TIMEOUT_MS = 1_000;

    private final BooleanSupplier condition;

    public ThrowWhenTimedOutBeforeMeeting(final int timeoutMs, final BooleanSupplier condition)
    {
        final long startTime = System.currentTimeMillis();
        this.condition = () ->
        {
            final boolean conditionMet = condition.getAsBoolean();
            if (conditionMet)
            {
                return true;
            }
            final boolean hasTimedOut = startTime + timeoutMs <= System.currentTimeMillis();
            if (!hasTimedOut)
            {
                return false;
            }
            throw new RuntimeException("Not completed within " + timeoutMs + "ms");
        };
    }

    public static BooleanSupplier timeoutOr(final BooleanSupplier condition)
    {
        return timeoutOr(DEFAULT_TIMEOUT_MS, condition);
    }

    public static BooleanSupplier timeoutOr(final int timeoutMs, final BooleanSupplier condition)
    {
        return new ThrowWhenTimedOutBeforeMeeting(timeoutMs, condition);
    }

    @Override
    public boolean getAsBoolean()
    {
        return condition.getAsBoolean();
    }
}
