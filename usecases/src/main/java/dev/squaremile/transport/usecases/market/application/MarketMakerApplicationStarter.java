package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

public class MarketMakerApplicationStarter
{
    private final InitiatorStarter initiatorStarter;
    private MarketMakerApplication marketMakerApplication;

    public MarketMakerApplicationStarter(final String remoteHost, final int remotePort, final Clock clock)
    {
        initiatorStarter = new InitiatorStarter(remoteHost, remotePort, (connectionTransport, connectionId) ->
        {
            marketMakerApplication = new MarketMakerApplication(new MarketMakerPublisher(connectionTransport));
            return new MarketTransportApplication(clock, marketMakerApplication);
        });
    }

    public TransportApplicationOnDuty startTransport(final Runnable runUntilReady, final int timeoutMs)
    {
        return initiatorStarter.startTransport(runUntilReady, timeoutMs);
    }

    public MarketMakerApplication application()
    {
        return marketMakerApplication;
    }
}
