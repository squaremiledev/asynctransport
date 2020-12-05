package dev.squaremile.transport.usecases.market;

public class ChangeSensitiveSecurityTicker implements FakeMarket.TickListener
{
    private final FakeMarket.TickListener listener;
    private final TrackedSecurity lastAnnouncedSecurityUpdate = new TrackedSecurity();
    private final int maxQuietPeriod;

    public ChangeSensitiveSecurityTicker(final int maxQuietPeriod, final FakeMarket.TickListener listener)
    {
        this.listener = listener;
        this.maxQuietPeriod = maxQuietPeriod;
    }

    @Override
    public void onTick(final Security security)
    {
        if (lastAnnouncedSecurityUpdate.midPrice() != security.midPrice() || lastAnnouncedSecurityUpdate.lastUpdateTime() + maxQuietPeriod < security.lastUpdateTime())
        {
            listener.onTick(security);
            lastAnnouncedSecurityUpdate.update(security);
        }
    }
}
