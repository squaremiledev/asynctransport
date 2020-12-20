package dev.squaremile.transport.usecases.market.domain;

public class TrackedSecurity implements Security
{
    private long updateTime;
    private long currentMidPrice;
    private long lastPriceChange;

    public TrackedSecurity()
    {
        this(0, 0, 0);
    }

    public TrackedSecurity(final long updateTime, final long currentMidPrice, final long lastPriceChange)
    {
        this.updateTime = updateTime;
        this.currentMidPrice = currentMidPrice;
        this.lastPriceChange = lastPriceChange;
    }

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

    @Override
    public long lastPriceChange()
    {
        return lastPriceChange;
    }

    public Security update(final Security source)
    {
        this.currentMidPrice = source.midPrice();
        this.updateTime = source.lastUpdateTime();
        this.lastPriceChange = source.lastPriceChange();
        return this;
    }

    public TrackedSecurity update(final long updateTime, final long currentMidPrice, final long lastPriceChange)
    {
        this.updateTime = updateTime;
        this.currentMidPrice = currentMidPrice;
        this.lastPriceChange = lastPriceChange;
        return this;
    }

    public TrackedSecurity midPrice(final long currentTime, final long currentMidPrice)
    {
        if (this.lastPriceChange == 0 || this.currentMidPrice != currentMidPrice)
        {
            this.currentMidPrice = currentMidPrice;
            this.lastPriceChange = currentTime;
        }
        this.updateTime = currentTime;
        return this;
    }

    @Override
    public String toString()
    {
        return "TrackedSecurity{" +
               "updateTime=" + updateTime +
               ", currentMidPrice=" + currentMidPrice +
               ", lastPriceChange=" + lastPriceChange +
               '}';
    }
}
