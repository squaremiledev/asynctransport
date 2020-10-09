package dev.squaremile.asynctcpacceptance.sampleapps.fix;


import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.CommandFailed;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.FIX_MESSAGES;

public class FixTransportApp implements Application
{
    private final Acceptor acceptor;
    private final Initiator initiator;
    private final Transport transport;
    private final int port;
    private final Runnable onMessage;
    private ConnectionId acceptorConnectionId;
    private ConnectionId initiatorConnectionId;

    public FixTransportApp(final Transport transport, final int port, final Runnable onMessage)
    {
        this.transport = transport;
        this.port = port;
        this.acceptor = new Acceptor(transport);
        this.initiator = new Initiator(transport);
        this.onMessage = onMessage;
    }

    @Override
    public void onStart()
    {
        transport.handle(transport.command(Listen.class).set(1, port, FIX_MESSAGES.type));
    }

    @Override
    public void onStop()
    {
        initiator.disconnect();
    }

    @Override
    public void work()
    {
        acceptor.work();
        initiator.work();
    }

    @Override
    public void onEvent(final Event event)
    {
//        System.out.println(event);
        if (event instanceof CommandFailed)
        {
            throw new IllegalStateException(event.toString());
        }
        else if (event instanceof StartedListening)
        {
            StartedListening startedListening = (StartedListening)event;
            transport.handle(transport.command(Connect.class).set("localhost", startedListening.port(), 2, 1_000, FIX_MESSAGES.type));
        }
        else if (event instanceof ConnectionAccepted)
        {
            ConnectionAccepted connectionAccepted = (ConnectionAccepted)event;
            this.acceptorConnectionId = new ConnectionIdValue(connectionAccepted);
            this.acceptor.onConnectionAccepted(connectionAccepted);
            this.acceptor.onEvent(connectionAccepted);
        }
        else if (event instanceof Connected)
        {
            Connected connected = (Connected)event;
            this.initiatorConnectionId = new ConnectionIdValue(connected);
            this.initiator.onConnected(connected);
            this.initiator.onEvent(connected);
        }
        else if (event instanceof ConnectionEvent)
        {
            if (event instanceof MessageReceived)
            {
                onMessage.run();
            }

            ConnectionEvent connectionEvent = (ConnectionEvent)event;

            if (acceptorConnectionId != null && connectionEvent.connectionId() == acceptorConnectionId.connectionId())
            {
                acceptor.onEvent(connectionEvent);
            }
            if (initiatorConnectionId != null && connectionEvent.connectionId() == initiatorConnectionId.connectionId())
            {
                initiator.onEvent(connectionEvent);
            }
        }
    }
}
