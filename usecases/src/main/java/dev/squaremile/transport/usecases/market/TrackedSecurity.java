package dev.squaremile.transport.usecases.market;

public class TrackedSecurity implements Security
{
    private long updateTime;
    private long currentMidPrice;
    private long lastPriceChange;

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

    public void update(final Security source)
    {
        this.currentMidPrice = source.midPrice();
        this.updateTime = source.lastUpdateTime();
        this.lastPriceChange = source.lastPriceChange();
    }

    public Security midPrice(final long currentTime, final long currentMidPrice)
    {
        if (this.currentMidPrice != currentMidPrice)
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
