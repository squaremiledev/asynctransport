package dev.squaremile.transport.usecases.market.domain;

@FunctionalInterface
public interface OrderResultListener
{
    OrderResultListener IGNORE = (marketParticipant, orderResult) ->
    {
    };

    void onOrderResult(final int marketParticipantId, OrderResult orderResult);
}
