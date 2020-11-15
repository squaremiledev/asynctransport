package dev.squaremile.asynctcp.api.wiring;

import java.util.ArrayList;
import java.util.List;


import dev.squaremile.asynctcp.transport.api.app.CommandFailed;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

public class ListeningApplication implements EventDrivenApplication
{

    private final Transport transport;
    private final ConnectionApplicationFactory connectionApplicationFactory;
    private final int port;
    private final Delineation delineation;
    private final List<SingleConnectionApplication> connectionApplications = new ArrayList<>(2);
    private final EventListener eventListener;

    public ListeningApplication(
            final Transport transport,
            final Delineation delineation,
            final int port,
            final EventListener eventListener,
            final ConnectionApplicationFactory connectionApplicationFactory
    )
    {
        this.transport = transport;
        this.connectionApplicationFactory = connectionApplicationFactory;
        this.port = port;
        this.delineation = delineation;
        this.eventListener = eventListener;
    }

    private static boolean matches(final SingleConnectionApplication connectedApplication, final ConnectionEvent event)
    {
        return connectedApplication.connectionId.connectionId() == event.connectionId();
    }

    @Override
    public void onStart()
    {
        transport.handle(transport.command(Listen.class).set(1, port, delineation));
    }

    @Override
    public void onStop()
    {
        connectionApplications.forEach(
                c ->
                {
                    try
                    {
                        c.application.onStop();
                    }
                    catch (final Exception e)
                    {
                        e.printStackTrace();
                    }
                });
        connectionApplications.clear();
    }

    @Override
    public void work()
    {
        for (SingleConnectionApplication connectedApplication : connectionApplications)
        {
            connectedApplication.application.work();
        }
    }

    @Override
    public void onEvent(final Event event)
    {
        eventListener.onEvent(event);
        if (event instanceof CommandFailed)
        {
            throw new IllegalStateException(event.toString());
        }
        if (event instanceof ConnectionAccepted)
        {
            ConnectionAccepted connectionAccepted = (ConnectionAccepted)event;
            ConnectionApplication newConnectionApplication = connectionApplicationFactory.create(
                    new SingleConnectionTransport(transport, connectionAccepted),
                    connectionAccepted
            );
            connectionApplications.add(new SingleConnectionApplication(connectionAccepted, newConnectionApplication));
            newConnectionApplication.onStart();
        }
        else if (event instanceof ConnectionResetByPeer || event instanceof ConnectionClosed)
        {
            ConnectionEvent closingEvent = (ConnectionEvent)event;
            for (final SingleConnectionApplication connectionApplication : connectionApplications)
            {
                if (matches(connectionApplication, closingEvent))
                {
                    connectionApplication.application.onStop();
                }
            }
            connectionApplications.removeIf(connectionApplication -> matches(connectionApplication, closingEvent));
        }
        else if (event instanceof ConnectionEvent)
        {
            ConnectionEvent connectionEvent = (ConnectionEvent)event;
            for (final SingleConnectionApplication connectionApplication : connectionApplications)
            {
                if (matches(connectionApplication, connectionEvent))
                {
                    connectionApplication.application.onEvent(connectionEvent);
                }
            }
        }
    }
}
