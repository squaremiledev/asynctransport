package dev.squaremile.asynctcpacceptance;

import dev.squaremile.asynctcp.api.transport.app.ConnectionApplication;
import dev.squaremile.asynctcp.api.transport.app.ConnectionEvent;
import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;
import dev.squaremile.tcpprobe.ProbeClient;

public class EchoConnectionApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;
    private final ProbeClient probeClient;


    public EchoConnectionApplication(final ConnectionTransport connectionTransport)
    {
        this.connectionTransport = connectionTransport;
        this.probeClient = new ProbeClient();
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)event;
            SendMessage message = connectionTransport.command(SendMessage.class);
            int outboundPayloadLength = probeClient.onMessage(
                    messageReceived.buffer(),
                    messageReceived.offset(),
                    message.prepare(),
                    message.offset()
            );
            if (outboundPayloadLength > 0)
            {
                message.commit(outboundPayloadLength);
                connectionTransport.handle(message);
            }
        }
    }
}
