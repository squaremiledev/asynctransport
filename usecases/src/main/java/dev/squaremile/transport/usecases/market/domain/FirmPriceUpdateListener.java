package dev.squaremile.transport.usecases.market.domain;

@FunctionalInterface
public interface FirmPriceUpdateListener
{
    FirmPriceUpdateListener IGNORE = (marketParticipant, firmPrice) ->
    {
    };

    void onFirmPriceUpdate(final int marketMakerId, FirmPrice firmPrice);
}
