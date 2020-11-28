package dev.squaremile.asynctcpacceptance;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

import static dev.squaremile.asynctcpacceptance.AdHocProtocol.CORRELATION_ID_OFFSET;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.NO_OPTIONS;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.OFFSET_OPTIONS;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.PLEASE_RESPOND_FLAG;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.SEND_TIME_OFFSET;

public class EchoConnectionApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;

    public EchoConnectionApplication(final ConnectionTransport connectionTransport)
    {
        this.connectionTransport = connectionTransport;
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)event;
            DirectBuffer readBuffer = messageReceived.buffer();
            boolean shouldRespond = readBuffer.getInt(messageReceived.offset() + OFFSET_OPTIONS) == PLEASE_RESPOND_FLAG;
            if (shouldRespond)
            {
                long sendTimeNs = readBuffer.getLong(messageReceived.offset() + SEND_TIME_OFFSET);
                long correlationId = readBuffer.getLong(messageReceived.offset() + CORRELATION_ID_OFFSET);
                SendMessage message = connectionTransport.command(SendMessage.class);
                MutableDirectBuffer buffer = message.prepare();
                buffer.putInt(message.offset() + OFFSET_OPTIONS, NO_OPTIONS);
                buffer.putLong(message.offset() + SEND_TIME_OFFSET, sendTimeNs);
                buffer.putLong(message.offset() + CORRELATION_ID_OFFSET, correlationId);
                message.commit(CORRELATION_ID_OFFSET + 8);
                connectionTransport.handle(message);
            }
        }
    }
}
