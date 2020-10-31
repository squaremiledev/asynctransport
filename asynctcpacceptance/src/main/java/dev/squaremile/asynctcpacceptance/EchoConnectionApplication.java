package dev.squaremile.asynctcpacceptance;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.api.AsyncTcp;
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
        if (args.length != 1)
        {
            System.out.println("Provide a port to listen on");
            return;
        }
        ApplicationOnDuty echo = echoApplication(parseInt(args[0]));
        echo.onStart();
        while (!Thread.interrupted())
        {
            echo.work();
        }
    }

    public static ApplicationOnDuty echoApplication(final int port)
    {
        return new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
                "echo",
                transport -> new ListeningApplication(
                        transport,
                        new Delineation(Delineation.Type.FIXED_LENGTH, 0, 16, ""),
                        port,
                        () ->
                        {
                        },
                        EchoConnectionApplication::new
                )
        );
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
            boolean shouldRespond = readBuffer.getLong(messageReceived.offset()) == PLEASE_RESPOND_FLAG;
            if (shouldRespond)
            {
                long sendTimeNs = readBuffer.getLong(messageReceived.offset() + 8);
                SendMessage message = connectionTransport.command(SendMessage.class);
                MutableDirectBuffer buffer = message.prepare();
                buffer.putLong(message.offset(), NO_OPTIONS);
                buffer.putLong(message.offset() + 8, sendTimeNs);
                message.commit(16);
                connectionTransport.handle(message);
            }
        }
    }
}
