package dev.squaremile.asynctcpacceptance;

import java.util.ArrayList;
import java.util.List;


import dev.squaremile.asynctcp.transport.api.app.CommandFailed;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.values.Delineation;
import dev.squaremile.asynctcpacceptance.demo.ConnectionApplicationFactory;
import dev.squaremile.asynctcpacceptance.demo.SingleLocalConnectionDemoApplication;

public class ListeningApplication implements EventDrivenApplication
{

    private final Transport transport;
    private final Runnable onReady;
    private final ConnectionApplicationFactory connectionApplicationFactory;
    private final int port;
    private final Delineation delineation;
    private final List<ConnectionApplication> connectionApplications = new ArrayList<>(2);

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

    private static boolean matches(final ConnectionApplication connectedApplication, final ConnectionEvent event)
    {
        return connectedApplication.connectionId().connectionId() == event.connectionId();
    }

    @Override
    public void onStart()
    {
        transport.handle(transport.command(Listen.class).set(1, port, delineation));
    }

    @Override
    public void onStop()
    {
        connectionApplications.forEach(ConnectionApplication::onStop);
        connectionApplications.clear();
    }

    @Override
    public void work()
    {
        for (ConnectionApplication connectedApplication : connectionApplications)
        {
            connectedApplication.work();
        }
    }

    @Override
    public void onEvent(final Event event)
    {
        if (event instanceof CommandFailed)
        {
            throw new IllegalStateException(event.toString());
        }
        if (event instanceof StartedListening)
        {
            onReady.run();
        }
        if (event instanceof ConnectionAccepted)
        {
            System.out.println("Accepted connection " + event);
            ConnectionAccepted connectionAccepted = (ConnectionAccepted)event;
            ConnectionApplication newConnectionApplication = connectionApplicationFactory.create(
                    new SingleLocalConnectionDemoApplication.SingleConnectionTransport(transport, connectionAccepted),
                    connectionAccepted
            );
            connectionApplications.add(newConnectionApplication);
            newConnectionApplication.onStart();
        }
        else if (event instanceof ConnectionResetByPeer || event instanceof ConnectionClosed)
        {
            System.out.println("Connection closed " + event);
            ConnectionEvent closingEvent = (ConnectionEvent)event;
            for (final ConnectionApplication connectionApplication : connectionApplications)
            {
                if (matches(connectionApplication, closingEvent))
                {
                    connectionApplication.onStop();
                }
            }
            connectionApplications.removeIf(connectionApplication -> matches(connectionApplication, closingEvent));
        }
        else if (event instanceof ConnectionEvent)
        {
            ConnectionEvent connectionEvent = (ConnectionEvent)event;
            for (final ConnectionApplication connectionApplication : connectionApplications)
            {
                if (matches(connectionApplication, connectionEvent))
                {
                    connectionApplication.onEvent(connectionEvent);
                }
            }
        }
    }
}