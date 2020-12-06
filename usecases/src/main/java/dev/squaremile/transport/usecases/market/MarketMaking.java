package dev.squaremile.transport.usecases.market;

import java.util.function.IntFunction;

import org.agrona.collections.Int2ObjectHashMap;

public class MarketMaking
{
    private static final IntFunction<FirmPrice> NO_FIRM_PRICE = participant -> FirmPrice.createNoPrice();
    private final Int2ObjectHashMap<FirmPrice> participantFirmPrice = new Int2ObjectHashMap<>();
    private int knownMarketParticipantId;

    public FirmPrice firmPrice(final int marketParticipantId)
    {
        return participantFirmPrice.computeIfAbsent(marketParticipantId, NO_FIRM_PRICE);
    }

    public void updateFirmPrice(final int marketParticipantId, final FirmPrice marketMakerFirmPrice)
    {
        firmPrice(marketParticipantId).update(marketMakerFirmPrice);
        this.knownMarketParticipantId = marketParticipantId;
    }

    public boolean execute(final FirmPrice executedQuantity)
    {
        return firmPrice(knownMarketParticipantId).execute(executedQuantity);
    }
}
