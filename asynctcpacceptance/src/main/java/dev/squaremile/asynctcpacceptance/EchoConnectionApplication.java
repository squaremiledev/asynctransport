package dev.squaremile.asynctcpacceptance;

import dev.squaremile.asynctcp.api.transport.app.ConnectionApplication;
import dev.squaremile.asynctcp.api.transport.app.ConnectionEvent;
import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;
import dev.squaremile.tcpprobe.Metadata;

public class EchoConnectionApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;
    private final Metadata metadata;

    public EchoConnectionApplication(final ConnectionTransport connectionTransport)
    {
        this.connectionTransport = connectionTransport;
        this.metadata = new Metadata();
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)event;
            metadata.wrap(messageReceived.buffer(), messageReceived.offset());
            if (metadata.options().respond())
            {
                long sendTimeNs = metadata.originalTimestampNs();
                long correlationId = metadata.correlationId();

                SendMessage message = connectionTransport.command(SendMessage.class);
                metadata.wrap(message.prepare(), message.offset())
                        .clear()
                        .originalTimestampNs(sendTimeNs)
                        .correlationId(correlationId);

                message.commit(metadata.length());
                connectionTransport.handle(message);
            }
        }
    }
}
