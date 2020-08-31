package dev.squaremile.asynctcpacceptance.sampleapps;

import dev.squaremile.asynctcp.application.Application;
import dev.squaremile.asynctcp.domain.api.Transport;
import dev.squaremile.asynctcp.domain.api.commands.Listen;
import dev.squaremile.asynctcp.domain.api.commands.StopListening;
import dev.squaremile.asynctcp.domain.api.events.Event;
import dev.squaremile.asynctcp.domain.api.events.StartedListening;
import dev.squaremile.asynctcp.domain.api.events.StoppedListening;

public class StreamEchoApplication implements Application
{
    private final Transport transport;
    private final int listeningPort;
    private boolean listening = false;

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
        if (listening)
        {
            transport.handle(transport.command(StopListening.class).set(2, listeningPort));
        }
    }

    @Override
    public void onEvent(final Event event)
    {
        if (event instanceof StartedListening)
        {
            listening = true;
        }
        if (event instanceof StoppedListening)
        {
            listening = false;
        }
    }
}
