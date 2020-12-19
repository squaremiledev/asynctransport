package dev.squaremile.transport.usecases.market.application;

public class Clock
{
    private long value;

    long currentTime()
    {
        return value;
    }

    void updateCurrentTime(long value)
    {
        this.value = value;
    }
}
