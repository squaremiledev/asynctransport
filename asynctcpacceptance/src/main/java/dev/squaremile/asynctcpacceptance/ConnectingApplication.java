package dev.squaremile.asynctcpacceptance;

import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.api.wiring.SingleConnectionTransport;
import dev.squaremile.asynctcp.transport.api.app.CommandFailed;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

public class ConnectingApplication implements EventDrivenApplication
{

    private final Transport transport;
    private final ConnectionApplicationFactory connectionApplicationFactory;
    private final int remotePort;
    private final Delineation delineation;
    private final String remoteHost;
    private ConnectionApplication connectionApplication;

    public ConnectingApplication(
            final Transport transport,
            final String remoteHost,
            final int remotePort,
            final Delineation delineation,
            final ConnectionApplicationFactory connectionApplicationFactory
    )
    {
        this.transport = transport;
        this.connectionApplicationFactory = connectionApplicationFactory;
        this.remotePort = remotePort;
        this.delineation = delineation;
        this.remoteHost = remoteHost;
    }

    @Override
    public void onStart()
    {
        transport.handle(transport.command(Connect.class).set(remoteHost, remotePort, 2, 1000, delineation));
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
        if (event instanceof CommandFailed)
        {
            throw new IllegalStateException(event.toString());
        }
        if (event instanceof Connected)
        {
            Connected connected = (Connected)event;
            connectionApplication = connectionApplicationFactory.create(
                    new SingleConnectionTransport(transport, connected),
                    connected
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
