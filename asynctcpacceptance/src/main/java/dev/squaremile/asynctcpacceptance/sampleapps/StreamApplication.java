package dev.squaremile.asynctcpacceptance.sampleapps;


import dev.squaremile.asynctcp.application.Application;
import dev.squaremile.asynctcp.domain.api.ConnectionIdValue;
import dev.squaremile.asynctcp.domain.api.Transport;
import dev.squaremile.asynctcp.domain.api.commands.CloseConnection;
import dev.squaremile.asynctcp.domain.api.commands.Connect;
import dev.squaremile.asynctcp.domain.api.commands.SendData;
import dev.squaremile.asynctcp.domain.api.events.Connected;
import dev.squaremile.asynctcp.domain.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.domain.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.domain.api.events.Event;
import dev.squaremile.asynctcp.domain.api.events.EventListener;

import static java.util.Objects.requireNonNull;

public class StreamApplication implements Application
{
    private final Transport transport;
    private final int remotePort;
    private final byte[] dataToSend;
    private final EventListener eventListener;
    private String remoteHost;
    private ConnectionIdValue connectionId;
    private long nextCommandId = 1;
    private long inFLightConnectCommandId = Long.MIN_VALUE;
    private boolean tryToConnect = false;

    public StreamApplication(
            final Transport transport,
            final String remoteHost,
            final int remotePort,
            final byte[] dataToSend,
            final EventListener eventListener
    )
    {
        this.transport = requireNonNull(transport);
        this.remoteHost = requireNonNull(remoteHost);
        this.remotePort = remotePort;
        this.dataToSend = dataToSend;
        this.eventListener = eventListener;
    }

    @Override
    public void onStart()
    {
        transport.handle(transport.command(Connect.class).set(remoteHost, remotePort, inFLightConnectCommandId, 50));
    }

    @Override
    public void onStop()
    {
        if (connectionId != null)
        {
            transport.handle(transport.command(connectionId, CloseConnection.class).set(nextCommandId++));
        }
    }

    @Override
    public void onEvent(final Event event)
    {
//        System.out.println("S@" + event);
        eventListener.onEvent(event);
        if (event instanceof Connected)
        {
            Connected connectedEvent = (Connected)event;
            transport.handle(transport.command(connectedEvent, SendData.class).set(dataToSend));
            connectionId = new ConnectionIdValue(connectedEvent);

        }
        else if (event instanceof ConnectionClosed || event instanceof ConnectionResetByPeer)
        {
            connectionId = null;
        }
    }
}
