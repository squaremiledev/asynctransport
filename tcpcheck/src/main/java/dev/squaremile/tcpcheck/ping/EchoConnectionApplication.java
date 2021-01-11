package dev.squaremile.tcpcheck.ping;

import dev.squaremile.asynctcp.api.transport.app.ConnectionApplication;
import dev.squaremile.asynctcp.api.transport.app.ConnectionEvent;
import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;
import dev.squaremile.tcpcheck.probe.ProbeClient;

import static dev.squaremile.tcpcheck.probe.Metadata.ALL_METADATA_FIELDS_TOTAL_LENGTH;
import static dev.squaremile.tcpcheck.probe.Metadata.DEFAULT_CORRELATION_ID_OFFSET;
import static dev.squaremile.tcpcheck.probe.Metadata.DEFAULT_OPTIONS_OFFSET;
import static dev.squaremile.tcpcheck.probe.Metadata.DEFAULT_SEND_TIME_OFFSET;

class EchoConnectionApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;
    private final ProbeClient probeClient;


    public EchoConnectionApplication(final ConnectionTransport connectionTransport)
    {
        this.connectionTransport = connectionTransport;
        this.probeClient = new ProbeClient(DEFAULT_OPTIONS_OFFSET, DEFAULT_SEND_TIME_OFFSET, DEFAULT_CORRELATION_ID_OFFSET);
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)event;
            SendMessage message = connectionTransport.command(SendMessage.class);
            boolean anythingToSend = probeClient.onMessage(
                    messageReceived.buffer(),
                    messageReceived.offset(),
                    message.prepare(),
                    message.offset(),
                    ALL_METADATA_FIELDS_TOTAL_LENGTH
            );
            if (anythingToSend)
            {
                message.commit(ALL_METADATA_FIELDS_TOTAL_LENGTH);
                connectionTransport.handle(message);
            }
        }
    }
}
