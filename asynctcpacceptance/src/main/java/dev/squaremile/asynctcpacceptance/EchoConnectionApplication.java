package dev.squaremile.asynctcpacceptance;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.serialization.internal.SerializedMessageListener.NO_OP;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.NO_OPTIONS;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.PLEASE_RESPOND_FLAG;
import static java.lang.Integer.parseInt;

public class EchoConnectionApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;
    private final ConnectionId connectionId;

    public EchoConnectionApplication(final ConnectionTransport connectionTransport, final ConnectionId connectionId)
    {
        this.connectionTransport = connectionTransport;
        this.connectionId = new ConnectionIdValue(connectionId);
    }

    public static void main(String[] args)
    {
        if (args.length != 1 && args.length != 2)
        {
            System.out.println("Usage: EchoConnectionApplication port(int) useBuffers(0|1) ");
            System.out.println("Example: EchoConnectionApplication 9998 1");
            return;
        }
        final boolean useBuffers = args.length > 1 && parseInt(args[1]) == 1;
        final ApplicationOnDuty app = echoApplication(parseInt(args[0]), useBuffers);
        app.onStart();
        while (!Thread.interrupted())
        {
            app.work();
        }
    }

    public static ApplicationOnDuty echoApplication(final int port, final boolean useBuffers)
    {
        return createApplication(useBuffers, transport -> new ListeningApplication(
                transport,
                new Delineation(Delineation.Type.INT_LITTLE_ENDIAN_FIELD, 0, 0, ""),
                port,
                () ->
                {
                },
                EchoConnectionApplication::new
        ));
    }

    private static ApplicationOnDuty createApplication(final boolean useBuffers, final ApplicationFactory applicationFactory)
    {
        TransportApplicationFactory transportApplicationFactory = new AsyncTcp().transportAppFactory(NON_PROD_GRADE);
        if (useBuffers)
        {
            System.out.println("Creating an app that uses ring buffers");
            return transportApplicationFactory.create("echo", 1024 * 1024, NO_OP, applicationFactory);
        }
        else
        {
            System.out.println("Creating an app without ring buffers");
            return transportApplicationFactory.createSharedStack("echo", applicationFactory);
        }
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
