package dev.squaremile.transport.casestudy.marketmaking.application;

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
