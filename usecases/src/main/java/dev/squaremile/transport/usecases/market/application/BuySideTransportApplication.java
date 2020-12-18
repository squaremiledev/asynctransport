package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;
import dev.squaremile.transport.usecases.market.domain.OrderResult;

class BuySideTransportApplication implements ConnectionApplication
{

    private final BuySideApplication application;
    private final Serialization serialization = new Serialization();

    public BuySideTransportApplication(final BuySideApplication application)
    {
        this.application = application;
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)event;
            MarketMessage marketMessage = serialization.decode(messageReceived.buffer(), messageReceived.offset());
            if (marketMessage instanceof OrderResult)
            {
                application.onOrderResult((OrderResult)marketMessage);
            }
        }
    }

}
