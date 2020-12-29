package dev.squaremile.transport.casestudy.marketmaking.application;

import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.transport.casestudy.marketmaking.domain.Exchange;
import dev.squaremile.transport.casestudy.marketmaking.domain.FirmPrice;
import dev.squaremile.transport.casestudy.marketmaking.domain.MarketMessage;
import dev.squaremile.transport.casestudy.marketmaking.domain.Order;

class ExchangeApplication implements MarketApplication
{
    private final Clock clock;
    private final Exchange exchange;
    private final ConnectionId connectionId;
    private final MarketParticipants marketParticipants;

    public ExchangeApplication(
            final ConnectionId connectionId,
            final Clock clock,
            final Exchange exchange,
            final MarketParticipants marketParticipants
    )
    {
        this.clock = clock;
        this.exchange = exchange;
        this.connectionId = new ConnectionIdValue(connectionId);
        this.marketParticipants = marketParticipants;
    }

    @Override
    public void onMessage(final MarketMessage marketMessage)
    {
        final long currentTime = clock.currentTime();
        final int marketParticipantId = marketParticipants.fromConnectionId(connectionId);

        if (marketMessage instanceof FirmPrice)
        {
            exchange.onFirmPriceUpdate(currentTime, marketParticipantId, (FirmPrice)marketMessage);
        }
        if (marketMessage instanceof Order)
        {
            exchange.execute(currentTime, marketParticipantId, (Order)marketMessage);
        }
    }

    public void work()
    {
        exchange.tick(clock.currentTime());
    }
}
