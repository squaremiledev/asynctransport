package dev.squaremile.transport.usecases.market;

import java.util.ArrayList;
import java.util.List;

public class TickerSpy implements TickListener
{
    final List<Security> observedTicks = new ArrayList<>();

    @Override
    public void onTick(final Security security)
    {
        observedTicks.add(new TrackedSecurity().update(security));
    }

    public List<Security> observedTicks()
    {
        return observedTicks;
    }

    public Security observedTick(final int index)
    {
        return observedTicks.get(index);
    }
}
