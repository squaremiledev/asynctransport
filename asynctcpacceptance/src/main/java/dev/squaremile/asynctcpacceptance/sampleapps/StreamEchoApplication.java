package dev.squaremile.asynctcpacceptance.sampleapps;

import dev.squaremile.asynctcp.application.Application;
import dev.squaremile.asynctcp.domain.api.Transport;
import dev.squaremile.asynctcp.domain.api.commands.Listen;
import dev.squaremile.asynctcp.domain.api.commands.StopListening;
import dev.squaremile.asynctcp.domain.api.events.Event;

public class StreamEchoApplication implements Application
{
    private final Transport transport;
    private final int listeningPort;

    public StreamEchoApplication(final Transport transport, final int listeningPort)
    {

        this.transport = transport;
        this.listeningPort = listeningPort;
    }

    @Override
    public void onStart()
    {
        transport.handle(transport.command(Listen.class).set(1, listeningPort));
    }

    @Override
    public void onStop()
    {
        transport.handle(transport.command(StopListening.class).set(2, listeningPort));
    }

    @Override
    public void onEvent(final Event event)
    {
    }
}
