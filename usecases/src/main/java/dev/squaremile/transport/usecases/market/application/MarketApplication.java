package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.transport.usecases.market.domain.FakeMarket;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;
import dev.squaremile.transport.usecases.market.domain.Order;

class MarketApplication implements BusinessApplication
{
    private final Clock clock;
    private final FakeMarket fakeMarket;
    private final ConnectionId connectionId;
    private final MarketParticipants marketParticipants;

    public MarketApplication(
            final ConnectionId connectionId,
            final Clock clock,
            final FakeMarket fakeMarket,
            final MarketParticipants marketParticipants
    )
    {
        this.clock = clock;
        this.fakeMarket = fakeMarket;
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
            fakeMarket.onFirmPriceUpdate(currentTime, marketParticipantId, (FirmPrice)marketMessage);
        }
        if (marketMessage instanceof Order)
        {
            fakeMarket.execute(currentTime, marketParticipantId, (Order)marketMessage);
        }
    }

    public void onPeriodicWakeUp()
    {
        fakeMarket.tick(clock.currentTime());
    }
}
