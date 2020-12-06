package dev.squaremile.transport.usecases.market;

public class PeriodicMidPriceChange implements MidPriceUpdate
{
    private final int period;
    private final int delta;

    public PeriodicMidPriceChange(final int period, final int delta)
    {
        this.period = period;
        this.delta = delta;
    }

    @Override
    public long newMidPrice(final long currentTime, final Security security)
    {
        return security.midPrice() + (currentTime - security.lastPriceChange() >= period ? delta : 0);
    }
}
