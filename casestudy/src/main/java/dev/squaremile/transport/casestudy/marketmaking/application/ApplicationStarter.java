package dev.squaremile.transport.casestudy.marketmaking.application;

import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public class ApplicationStarter<T extends MarketApplication>
{
    private final InitiatorStarter initiatorStarter;
    private MarketConnectionApplication<T> marketConnectionApplication;

    public ApplicationStarter(final String remoteHost, final int remotePort, final Clock clock, final MarketApplicationFactory<T> marketApplicationFactory)
    {
        initiatorStarter = new InitiatorStarter(
                "marketMaker",
                remoteHost,
                remotePort,
                (connectionTransport, connectionId) ->
                {
                    marketConnectionApplication = new MarketConnectionApplication<>(clock, marketApplicationFactory.create(connectionTransport, connectionId));
                    return marketConnectionApplication;
                }
        );
    }

    public TransportApplicationOnDuty startTransport(final int timeoutMs)
    {
        return startTransport(() ->
                              {
                              }, timeoutMs);
    }

    public TransportApplicationOnDuty startTransport(final Runnable runUntilReady, final int timeoutMs)
    {
        return initiatorStarter.startTransport(runUntilReady, timeoutMs);
    }

    public T application()
    {
        return marketConnectionApplication == null ? null : marketConnectionApplication.application();
    }

    public interface MarketApplicationFactory<T extends MarketApplication>
    {
        T create(ConnectionTransport connectionTransport, ConnectionId connectionId);
    }
}
