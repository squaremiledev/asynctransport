package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;

import static java.lang.System.currentTimeMillis;

class MarketTransportApplication implements ConnectionApplication
{
    private final Clock clock;
    private final Serialization serialization = new Serialization();
    private final BusinessApplication application;

    public MarketTransportApplication(final Clock clock, final BusinessApplication application)
    {
        this.clock = clock;
        this.application = application;
    }

    @Override
    public void onEvent(final ConnectionEvent connectionEvent)
    {
        clock.updateCurrentTimeMs(currentTimeMillis());
        if (connectionEvent instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)connectionEvent;
            MarketMessage marketMessage = serialization.decode(messageReceived.buffer(), messageReceived.offset());
            application.onMessage(marketMessage);
        }
    }

    @Override
    public void work()
    {
        clock.updateCurrentTimeMs(currentTimeMillis());
        application.onPeriodicWakeUp();
    }
}
