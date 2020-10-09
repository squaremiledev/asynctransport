package dev.squaremile.asynctcpacceptance.sampleapps.fix;

import java.nio.charset.StandardCharsets;


import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionEventsListener;

public class Initiator implements ConnectionEventsListener
{
    private final Transport transport;
    private final byte[] logonMessage = asciiFix("8=FIXT.1.1^9=116^35=A^49=BuySide^56=SellSide^34=1^" +
                                                 "52=20190605-11:51:27.848^1128=9^98=0^108=30^141=Y^" +
                                                 "553=Username^554=Password^1137=9^10=079^");
    private Connected connected;


    public Initiator(final Transport transport)
    {
        this.transport = transport;
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
//        System.out.println("CLIENT: " + event);
        if (event instanceof Connected || event instanceof MessageReceived)
        {
            SendMessage sendMessage = transport.command(event, SendMessage.class);
            sendMessage.prepare().putBytes(sendMessage.offset(), logonMessage);
            sendMessage.commit(logonMessage.length);
            transport.handle(sendMessage);
        }
    }

    public void disconnect()
    {
        if (connected != null)
        {
            transport.handle(transport.command(connected, CloseConnection.class));
        }
    }

    public void onConnected(final Connected connected)
    {
        this.connected = connected.copy();
    }

    private static byte[] asciiFix(final String content)
    {
        return content.replaceAll("\\^", "\u0001").getBytes(StandardCharsets.US_ASCII);
    }
}
