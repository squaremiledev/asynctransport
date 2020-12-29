package dev.squaremile.transport.casestudy.marketmaking.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.transport.casestudy.marketmaking.domain.CurrentTime;
import dev.squaremile.transport.casestudy.marketmaking.domain.MarketMessage;

class MarketConnectionApplication<T extends MarketApplication> implements ConnectionApplication
{
    private final Clock clock;
    private final Serialization serialization = new Serialization();
    private final T application;

    public MarketConnectionApplication(final Clock clock, final T application)
    {
        this.clock = clock;
        this.application = application;
    }

    @Override
    public void onEvent(final ConnectionEvent connectionEvent)
    {
        clock.updateCurrentTime(CurrentTime.currentTime());
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
        clock.updateCurrentTime(CurrentTime.currentTime());
        application.work();
    }

    public T application()
    {
        return application;
    }
}
