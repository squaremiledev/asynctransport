package dev.squaremile.transport.casestudy.marketmaking.domain;

@FunctionalInterface
public interface OrderResultListener
{
    OrderResultListener IGNORE = (marketParticipant, orderResult) ->
    {
    };

    void onOrderResult(final int marketParticipantId, OrderResult orderResult);
}
