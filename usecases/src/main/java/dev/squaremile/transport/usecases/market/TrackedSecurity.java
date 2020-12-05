package dev.squaremile.transport.usecases.market;

public class TrackedSecurity implements Security
{
    private long updateTime;
    private long currentMidPrice;

    @Override
    public long midPrice()
    {
        return currentMidPrice;

    }

    @Override
    public long lastUpdateTime()
    {
        return updateTime;
    }

    public Security update(final Security source)
    {
        this.currentMidPrice = source.midPrice();
        this.updateTime = source.lastUpdateTime();
        return this;
    }

    public Security midPrice(final long currentTime, final long currentMidPrice)
    {
        this.updateTime = currentTime;
        this.currentMidPrice = currentMidPrice;
        return this;
    }
}
