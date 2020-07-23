package com.michaelszymczak.sample.sockets.domain.connection;

import com.michaelszymczak.sample.sockets.domain.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionClosed;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionCommandFailed;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionEvent;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionResetByPeer;
import com.michaelszymczak.sample.sockets.domain.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.domain.api.events.DataSent;

public class SingleConnectionEvents implements ConnectionEventsListener
{

    private final ConnectionEventsListener eventsListener;
    private final int port;
    private final long connectionId;
    private final DataReceived dataReceivedEvent;
    private final DataSent dataSentEvent;

    public SingleConnectionEvents(final ConnectionEventsListener eventsListener, final int port, final long connectionId, final int maxInboundMessageSize)
    {
        this.eventsListener = eventsListener;
        this.port = port;
        this.connectionId = connectionId;
        this.dataReceivedEvent = new DataReceived(port, connectionId, maxInboundMessageSize);
        this.dataSentEvent = new DataSent(this.port, this.connectionId);
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        eventsListener.onEvent(event);
    }

    public void commandFailed(final ConnectionCommand command, final String reason)
    {
        onEvent(new ConnectionCommandFailed(command, reason + " (" + command.getClass().getSimpleName() + ")"));
    }

    public void connectionClosed(final long commandId)
    {
        onEvent(new ConnectionClosed(port, connectionId, commandId));
    }

    public void connectionResetByPeer(final long commandId)
    {
        onEvent(new ConnectionResetByPeer(port, connectionId, commandId));
    }

    public DataReceived dataReceivedEvent()
    {
        return dataReceivedEvent;
    }

    public DataSent dataSentEvent()
    {
        return dataSentEvent;
    }
}
