package dev.squaremile.asynctcpacceptance.sampleapps.fix;

import java.nio.charset.StandardCharsets;


import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionEventsListener;

public class Acceptor implements ConnectionEventsListener
{
    private final Transport transport;
    private final byte[] logoutMessage = asciiFix("8=FIX.4.2^9=84^35=5^49=SellSide^" +
                                                  "56=BuySide^34=3^52=20190606-09:25:34.329^" +
                                                  "58=Logout acknowledgement^10=049^");

    public Acceptor(final Transport transport)
    {
        this.transport = transport;
    }

    private static byte[] asciiFix(final String content)
    {
        return content.replaceAll("\\^", "\u0001").getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
//        System.out.println("SERVER: " + event);
        if (event instanceof MessageReceived)
        {
            SendMessage sendMessage = transport.command(event, SendMessage.class);
            sendMessage.prepare().putBytes(sendMessage.offset(), logoutMessage);
            sendMessage.commit(logoutMessage.length);
            transport.handle(sendMessage);
        }
    }

    public void onConnectionAccepted(final ConnectionAccepted connectionAccepted)
    {
    }
}
