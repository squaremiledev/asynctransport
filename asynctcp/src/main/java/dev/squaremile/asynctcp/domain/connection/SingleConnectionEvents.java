package dev.squaremile.asynctcp.domain.connection;

import dev.squaremile.asynctcp.domain.api.commands.ConnectionCommand;
import dev.squaremile.asynctcp.domain.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.domain.api.events.ConnectionCommandFailed;
import dev.squaremile.asynctcp.domain.api.events.ConnectionEvent;
import dev.squaremile.asynctcp.domain.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.DataSent;

public class SingleConnectionEvents implements ConnectionEventsListener
{

    private final ConnectionEventsListener eventsListener;
    private final int port;
    private final long connectionId;
    private final DataReceived dataReceivedEvent;
    private final DataSent dataSentEvent;

    public SingleConnectionEvents(final ConnectionEventsListener eventsListener, final int port, final long connectionId, final int inboundPduLimit)
    {
        this.eventsListener = eventsListener;
        this.port = port;
        this.connectionId = connectionId;
        this.dataReceivedEvent = new DataReceived(port, connectionId, inboundPduLimit);
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
