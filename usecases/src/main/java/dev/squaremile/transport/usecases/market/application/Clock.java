package dev.squaremile.transport.usecases.market.application;

public class Clock
{
    private long value;

    long currentTimeMs()
    {
        return value;
    }

    void updateCurrentTimeMs(long value)
    {
        this.value = value;
    }
}
