package dev.squaremile.asynctcpacceptance;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

import static dev.squaremile.asynctcpacceptance.AdHocProtocol.NO_OPTIONS;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.PLEASE_RESPOND_FLAG;

public class EchoConnectionApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;
    private final ConnectionId connectionId;

    public EchoConnectionApplication(final ConnectionTransport connectionTransport, final ConnectionId connectionId)
    {
        this.connectionTransport = connectionTransport;
        this.connectionId = new ConnectionIdValue(connectionId);
    }

    @Override
    public ConnectionId connectionId()
    {
        return connectionId;
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)event;
            DirectBuffer readBuffer = messageReceived.buffer();
            boolean shouldRespond = readBuffer.getInt(messageReceived.offset()) == PLEASE_RESPOND_FLAG;
            if (shouldRespond)
            {
                long sendTimeNs = readBuffer.getLong(messageReceived.offset() + 4);
                SendMessage message = connectionTransport.command(SendMessage.class);
                MutableDirectBuffer buffer = message.prepare(12);
                buffer.putInt(message.offset(), NO_OPTIONS);
                buffer.putLong(message.offset() + 4, sendTimeNs);
                message.commit();
                connectionTransport.handle(message);
            }
        }
    }
}
