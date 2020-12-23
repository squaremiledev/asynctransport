package dev.squaremile.transport.usecases.market.application;

import java.util.ArrayList;
import java.util.List;


import dev.squaremile.transport.usecases.market.domain.ExecutionReport;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.MarketListener;
import dev.squaremile.transport.usecases.market.domain.OrderResult;
import dev.squaremile.transport.usecases.market.domain.Security;
import dev.squaremile.transport.usecases.market.domain.TrackedSecurity;

public class MarketMakerChart implements MarketListener
{
    private final List<String> rows = new ArrayList<>();
    private final TimeUnitConversion timeUnitConversion;
    private long baseTime;
    private boolean baseTimeSet;
    private long lastUpdateTime = Long.MIN_VALUE;
    private final TrackedSecurity trackedSecurity = new TrackedSecurity();
    private final FirmPrice trackedFirmPrice = FirmPrice.createNoPrice();
    private final int coverArea;

    public MarketMakerChart()
    {
        this(time -> time, 0);
    }

    public MarketMakerChart(final TimeUnitConversion timeUnitConversion, final int coverArea)
    {
        this.timeUnitConversion = timeUnitConversion;
        this.coverArea = coverArea;
        rows.add("Time[s],Mid,Bid,Ask");
    }

    @Override
    public void onExecution(final ExecutionReport executionReport)
    {

    }

    @Override
    public void onFirmPriceUpdate(final int marketMakerId, final FirmPrice firmPrice)
    {
        long time = relativeTime(firmPrice.updateTime());
        if (time == lastUpdateTime)
        {
            return;
        }
        addEntry(trackedFirmPrice.update(time, firmPrice).updateTime());
    }

    @Override
    public void onOrderResult(final int marketParticipantId, final OrderResult orderResult)
    {

    }

    @Override
    public void onTick(final Security security)
    {
        long time = relativeTime(security.lastUpdateTime());
        if (time == lastUpdateTime)
        {
            return;
        }
        addEntry(trackedSecurity.midPrice(time, security.midPrice()).lastUpdateTime());
    }

    private void addEntry(final long updateTime)
    {
        rows.add(String.format("%d,%d;%d;%d,%d;%d;%d,%d;%d;%d",
                               updateTime,
                               trackedSecurity.midPrice(), trackedSecurity.midPrice(), trackedSecurity.midPrice(),
                               trackedFirmPrice.bidPrice() - coverArea, trackedFirmPrice.bidPrice(), trackedFirmPrice.bidPrice(),
                               trackedFirmPrice.askPrice(), trackedFirmPrice.askPrice(), trackedFirmPrice.askPrice() + coverArea
        ));
        lastUpdateTime = updateTime;
    }

    private long relativeTime(final long time)
    {
        if (!baseTimeSet)
        {
            baseTime = time;
            baseTimeSet = true;
        }
        return timeUnitConversion.convert(time - baseTime);
    }

    public String generateAsString()
    {
        return String.join("\n", rows);
    }

    interface TimeUnitConversion
    {
        long convert(long time);
    }
}
