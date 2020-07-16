package com.michaelszymczak.sample.sockets.nio;

import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.events.ConnectionClosed;
import com.michaelszymczak.sample.sockets.api.events.ConnectionCommandFailed;
import com.michaelszymczak.sample.sockets.api.events.ConnectionResetByPeer;
import com.michaelszymczak.sample.sockets.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.api.events.DataSent;
import com.michaelszymczak.sample.sockets.connection.ConnectionEventsListener;

public class ThisConnectionEvents
{

    private final ConnectionEventsListener eventsListener;
    private final int port;
    private final long connectionId;

    public ThisConnectionEvents(final ConnectionEventsListener eventsListener, final int port, final long connectionId)
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

    public void dataSent(final int bytesSent, final long totalBytesSent, final long commandId)
    {
        eventsListener.onEvent(new DataSent(port, connectionId, bytesSent, totalBytesSent, commandId));
    }

    public void dataReceived(final long totalBytesReceived, final byte[] content, final int read)
    {
        eventsListener.onEvent(new DataReceived(port, connectionId, totalBytesReceived, content, read));
    }
}
