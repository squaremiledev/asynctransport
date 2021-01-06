package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.api.transport.app.CommandFailed;
import dev.squaremile.asynctcp.api.transport.app.ConnectionApplication;
import dev.squaremile.asynctcp.api.transport.app.ConnectionEvent;
import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.Transport;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.commands.Connect;
import dev.squaremile.asynctcp.api.transport.events.Connected;
import dev.squaremile.asynctcp.api.transport.events.ConnectionClosed;
import dev.squaremile.asynctcp.api.transport.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.api.transport.values.Delineation;

public class ConnectingApplication implements TransportApplicationOnDuty
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
        final ConnectionApplication stoppingConnectionApplication = connectionApplication;
        if (stoppingConnectionApplication != null)
        {
            connectionApplication = null;
            stoppingConnectionApplication.onStop();
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
