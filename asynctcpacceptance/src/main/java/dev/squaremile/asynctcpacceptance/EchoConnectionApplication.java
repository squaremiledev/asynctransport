package dev.squaremile.asynctcpacceptance;

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

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.fixedLengthDelineation;
import static java.lang.Integer.parseInt;
import static java.lang.System.nanoTime;

public class EchoConnectionApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;
    private final ConnectionId connectionId;

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
                        fixedLengthDelineation(16),
                        port,
                        () ->
                        {
                        },
                        EchoConnectionApplication::new
                )
        );
    }

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
            long sendTimeNs = messageReceived.buffer().getLong(messageReceived.offset());
            SendMessage message = connectionTransport.command(SendMessage.class);
            MutableDirectBuffer buffer = message.prepare();
            buffer.putLong(message.offset(), sendTimeNs);
            buffer.putLong(message.offset() + 8, nanoTime());
            message.commit(16);
            connectionTransport.handle(message);
        }
    }
}
