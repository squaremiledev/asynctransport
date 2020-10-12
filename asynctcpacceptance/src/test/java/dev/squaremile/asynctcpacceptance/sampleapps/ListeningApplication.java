package dev.squaremile.asynctcpacceptance.sampleapps;

import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.StopListening;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.values.Delineation;
import dev.squaremile.asynctcpacceptance.demo.ConnectionApplicationFactory;
import dev.squaremile.asynctcpacceptance.demo.SingleLocalConnectionDemoApplication;

class ListeningApplication implements EventDrivenApplication
{

    private final Transport transport;
    private final Runnable onReady;
    private final ConnectionApplicationFactory connectionApplicationFactory;
    private final int port;
    private final Delineation delineation;
    private ConnectionApplication connectionApplication;

    public ListeningApplication(
            final Transport transport,
            final Delineation delineation,
            final int port,
            final Runnable onReady,
            final ConnectionApplicationFactory connectionApplicationFactory
    )
    {
        this.transport = transport;
        this.onReady = onReady;
        this.connectionApplicationFactory = connectionApplicationFactory;
        this.port = port;
        this.delineation = delineation;
    }

    @Override
    public void onStart()
    {
        transport.handle(transport.command(Listen.class).set(1, port, delineation));
    }

    @Override
    public void onStop()
    {
        if (connectionApplication != null)
        {
            connectionApplication.onStop();
            connectionApplication = null;
        }
    }

    @Override
    public void work()
    {
        if (connectionApplication != null)
        {
            connectionApplication.work();
        }
    }

    @Override
    public void onEvent(final Event event)
    {
        if (event instanceof StartedListening)
        {
            onReady.run();
        }
        if (event instanceof ConnectionAccepted)
        {
            transport.handle(transport.command(StopListening.class).set(3, port));
            ConnectionAccepted connectionAccepted = (ConnectionAccepted)event;
            connectionApplication = connectionApplicationFactory.create(
                    new SingleLocalConnectionDemoApplication.SingleConnectionTransport(transport, connectionAccepted),
                    connectionAccepted
            );
            connectionApplication.onStart();
        }
        else if (event instanceof ConnectionResetByPeer || event instanceof ConnectionClosed)
        {
            if (connectionApplication != null)
            {
                connectionApplication.onStop();
                connectionApplication = null;
            }
        }
        else if (connectionApplication != null && event instanceof ConnectionEvent)
        {
            connectionApplication.onEvent((ConnectionEvent)event);
        }
    }
}
