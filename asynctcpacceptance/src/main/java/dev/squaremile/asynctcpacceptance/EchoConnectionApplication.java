package dev.squaremile.asynctcpacceptance;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.api.transport.app.ConnectionApplication;
import dev.squaremile.asynctcp.api.transport.app.ConnectionEvent;
import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;
import dev.squaremile.tcpprobe.Options;

import static dev.squaremile.asynctcpacceptance.AdHocProtocol.CORRELATION_ID_OFFSET;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.NO_OPTIONS;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.OFFSET_OPTIONS;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.SEND_TIME_OFFSET;

public class EchoConnectionApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;
    private final Options options = new Options();

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
            options.wrap(messageReceived.buffer(), messageReceived.offset());
            DirectBuffer readBuffer = messageReceived.buffer();
            if (options.respond())
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
