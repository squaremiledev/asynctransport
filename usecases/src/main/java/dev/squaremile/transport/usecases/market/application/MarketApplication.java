package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.transport.usecases.market.domain.FakeMarket;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;
import dev.squaremile.transport.usecases.market.domain.Order;
import dev.squaremile.transport.usecases.market.domain.OrderResult;

class MarketApplication implements BusinessApplication
{
    private final FirmPrice firmPriceResponse = FirmPrice.createNoPrice();
    private final Clock clock;
    private final FakeMarket fakeMarket;
    private final MarketPublisher marketPublisher;
    private final ConnectionId connectionId;
    private final MarketParticipants marketParticipants;

    public MarketApplication(
            final ConnectionId connectionId,
            final ConnectionTransport connectionTransport,
            final Clock clock,
            final FakeMarket fakeMarket,
            final MarketParticipants marketParticipants
    )
    {
        this.clock = clock;
        this.fakeMarket = fakeMarket;
        this.marketPublisher = new MarketPublisher(connectionTransport);
        this.connectionId = new ConnectionIdValue(connectionId);
        this.marketParticipants = marketParticipants;
    }

    @Override
    public void onMessage(final MarketMessage marketMessage)
    {
        if (marketMessage instanceof FirmPrice)
        {
            FirmPrice firmPrice = (FirmPrice)marketMessage;
            fakeMarket.onFirmPriceUpdate(clock.currentTimeMs(), marketParticipants.fromConnectionId(connectionId), firmPrice);
            firmPriceResponse.update(clock.currentTimeMs(), firmPrice);
            marketPublisher.publish(firmPriceResponse);
        }
        if (marketMessage instanceof Order)
        {
            boolean executed = fakeMarket.execute(clock.currentTimeMs(), marketParticipants.fromConnectionId(connectionId), (Order)marketMessage);
            OrderResult orderResult = executed ? OrderResult.EXECUTED : OrderResult.NOT_EXECUTED;
            marketPublisher.publish(orderResult);
        }
    }

    public void onPeriodicWakeUp()
    {
        fakeMarket.tick(clock.currentTimeMs());
    }
}
