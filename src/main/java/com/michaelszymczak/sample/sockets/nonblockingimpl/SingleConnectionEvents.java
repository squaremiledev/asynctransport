package com.michaelszymczak.sample.sockets.nonblockingimpl;

import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.events.ConnectionClosed;
import com.michaelszymczak.sample.sockets.api.events.ConnectionCommandFailed;
import com.michaelszymczak.sample.sockets.api.events.ConnectionEvent;
import com.michaelszymczak.sample.sockets.api.events.ConnectionResetByPeer;
import com.michaelszymczak.sample.sockets.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.api.events.DataSent;
import com.michaelszymczak.sample.sockets.connection.ConnectionEventsListener;

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
