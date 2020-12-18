package dev.squaremile.transport.usecases.market.application;

import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

public class BuySideApplicationStarter
{
    private final InitiatorStarter initiatorStarter;
    private BuySideApplication application;

    public BuySideApplicationStarter(final String remoteHost, final int remotePort)
    {
        initiatorStarter = new InitiatorStarter(remoteHost, remotePort, (connectionTransport, connectionId) ->
        {
            application = new BuySideApplication(new BuySidePublisher(connectionTransport));
            return new BuySideTransportApplication(application);
        });
    }

    public TransportApplicationOnDuty startTransport(final Runnable runUntilReady, final int timeoutMs)
    {
        return initiatorStarter.startTransport(runUntilReady, timeoutMs);
    }

    public BuySideApplication application()
    {
        return application;
    }
}
