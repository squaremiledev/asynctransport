package dev.squaremile.transport.usecases.market.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Long> midPrices()
    {
        return observedTicks.stream().map(Security::midPrice).collect(Collectors.toList());
    }

    public Security observedTick(final int index)
    {
        return observedTicks.get(index);
    }
}
