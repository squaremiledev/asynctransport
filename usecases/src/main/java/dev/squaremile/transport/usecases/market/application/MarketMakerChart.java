package dev.squaremile.transport.usecases.market.application;

import java.util.ArrayList;
import java.util.List;


import dev.squaremile.transport.usecases.market.domain.ExecutionReport;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.MarketListener;
import dev.squaremile.transport.usecases.market.domain.OrderResult;
import dev.squaremile.transport.usecases.market.domain.Security;

public class MarketMakerChart implements MarketListener
{
    private final List<String> rows = new ArrayList<>();
    private final TimeUnitConversion timeUnitConversion;
    private long baseTime;
    private boolean baseTimeSet;

    public MarketMakerChart()
    {
        this(time -> time);
    }

    public MarketMakerChart(final TimeUnitConversion timeUnitConversion)
    {
        this.timeUnitConversion = timeUnitConversion;
        rows.add("Time[s],Mid,Bid,Ask");
    }

    @Override
    public void onExecution(final ExecutionReport executionReport)
    {

    }

    @Override
    public void onFirmPriceUpdate(final int marketMakerId, final FirmPrice firmPrice)
    {

    }

    @Override
    public void onOrderResult(final int marketParticipantId, final OrderResult orderResult)
    {

    }

    @Override
    public void onTick(final Security security)
    {
        long time = relativeTime(security.lastUpdateTime());
        long midPrice = security.midPrice();
        rows.add(String.format("%d,%d;%d;%d,0;0;0,0;0;0",
                               time,
                               midPrice, midPrice, midPrice
        ));
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
