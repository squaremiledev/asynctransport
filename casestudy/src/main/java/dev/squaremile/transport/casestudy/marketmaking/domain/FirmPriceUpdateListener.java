package dev.squaremile.transport.casestudy.marketmaking.domain;

@FunctionalInterface
public interface FirmPriceUpdateListener
{
    FirmPriceUpdateListener IGNORE = (marketParticipant, firmPrice) ->
    {
    };

    void onFirmPriceUpdate(final int marketMakerId, FirmPrice firmPrice);
}
