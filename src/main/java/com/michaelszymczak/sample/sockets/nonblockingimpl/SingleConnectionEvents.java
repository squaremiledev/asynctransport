package com.michaelszymczak.sample.sockets.nonblockingimpl;

import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.events.ConnectionClosed;
import com.michaelszymczak.sample.sockets.api.events.ConnectionCommandFailed;
import com.michaelszymczak.sample.sockets.api.events.ConnectionResetByPeer;
import com.michaelszymczak.sample.sockets.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.api.events.DataSent;
import com.michaelszymczak.sample.sockets.connection.ConnectionEventsListener;

public class SingleConnectionEvents
{

    private final ConnectionEventsListener eventsListener;
    private final int port;
    private final long connectionId;

    public SingleConnectionEvents(final ConnectionEventsListener eventsListener, final int port, final long connectionId)
    {
        this.eventsListener = eventsListener;
        this.port = port;
        this.connectionId = connectionId;
    }

    public void commandFailed(final ConnectionCommand command, final String reason)
    {
        eventsListener.onEvent(new ConnectionCommandFailed(command, reason + " (" + command.getClass().getSimpleName() + ")"));
    }

    public void connectionClosed(final long commandId)
    {
        eventsListener.onEvent(new ConnectionClosed(port, connectionId, commandId));
    }

    public void connectionResetByPeer(final long commandId)
    {
        eventsListener.onEvent(new ConnectionResetByPeer(port, connectionId, commandId));
    }

    // TODO: needs a similar approach that commands to reuse pre-created ones and avoid allocation
    public void dataSent(final int bytesSent, final long totalBytesSent, final long totalBytesBuffered, final long commandId)
    {
        eventsListener.onEvent(new DataSent(port, connectionId, bytesSent, totalBytesSent, totalBytesBuffered, commandId));
    }

    public void dataReceived(final long totalBytesReceived, final byte[] content, final int read)
    {
        eventsListener.onEvent(new DataReceived(port, connectionId, totalBytesReceived, content, read));
    }
}
