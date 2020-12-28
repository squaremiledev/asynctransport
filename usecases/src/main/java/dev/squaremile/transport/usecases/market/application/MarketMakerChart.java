package dev.squaremile.transport.usecases.market.application;

import org.agrona.ExpandableDirectByteBuffer;


import dev.squaremile.transport.usecases.market.domain.ExecutionReport;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.MarketListener;
import dev.squaremile.transport.usecases.market.domain.OrderResult;
import dev.squaremile.transport.usecases.market.domain.Security;
import dev.squaremile.transport.usecases.market.domain.TrackedSecurity;

public class MarketMakerChart implements MarketListener
{
    private final ExpandableDirectByteBuffer content = new ExpandableDirectByteBuffer();
    private final TimeUnitConversion timeUnitConversion;
    private final TrackedSecurity trackedSecurity = new TrackedSecurity();
    private final FirmPrice trackedFirmPrice = FirmPrice.createNoPrice();
    private final int coverArea;
    private int position = 0;
    private int entriesCaptured = 0;
    private long baseTime;
    private boolean baseTimeSet;
    private long lastUpdateTime = Long.MIN_VALUE;

    public MarketMakerChart()
    {
        this(time -> time, 0);
    }

    public MarketMakerChart(final TimeUnitConversion timeUnitConversion, final int coverArea)
    {
        this.timeUnitConversion = timeUnitConversion;
        this.coverArea = coverArea;
        position += content.putStringWithoutLengthAscii(position, "Time[s],Mid,Bid,Ask\n");
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
        position += content.putLongAscii(position, updateTime);
        position += content.putStringWithoutLengthAscii(position, ",");

        position += content.putLongAscii(position, trackedSecurity.midPrice());
        position += content.putStringWithoutLengthAscii(position, ";");

        position += content.putLongAscii(position, trackedSecurity.midPrice());
        position += content.putStringWithoutLengthAscii(position, ";");

        position += content.putLongAscii(position, trackedSecurity.midPrice());
        position += content.putStringWithoutLengthAscii(position, ",");

        position += content.putLongAscii(position, trackedFirmPrice.bidPrice() - coverArea);
        position += content.putStringWithoutLengthAscii(position, ";");

        position += content.putLongAscii(position, trackedFirmPrice.bidPrice());
        position += content.putStringWithoutLengthAscii(position, ";");

        position += content.putLongAscii(position, trackedFirmPrice.bidPrice());
        position += content.putStringWithoutLengthAscii(position, ",");

        position += content.putLongAscii(position, trackedFirmPrice.askPrice());
        position += content.putStringWithoutLengthAscii(position, ";");

        position += content.putLongAscii(position, trackedFirmPrice.askPrice());
        position += content.putStringWithoutLengthAscii(position, ";");

        position += content.putLongAscii(position, trackedFirmPrice.askPrice() + coverArea);
        position += content.putStringWithoutLengthAscii(position, "\n");
        this.lastUpdateTime = updateTime;
        this.entriesCaptured++;
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

    public int entriesCaptured()
    {
        return entriesCaptured;
    }

    public String generateAsString()
    {
        return content.getStringWithoutLengthAscii(0, position);
    }

    public byte[] generateAsStringBytes()
    {
        final byte[] contentAsBytes = new byte[position];
        content.getBytes(0, contentAsBytes);
        return contentAsBytes;
    }

    interface TimeUnitConversion
    {
        long convert(long time);
    }
}
