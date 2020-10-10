package dev.squaremile.asynctcpacceptance.sampleapps.fix;

import java.nio.charset.StandardCharsets;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.DataSent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

public class RejectLogOn implements ConnectionApplication
{
    private final Transport transport;
    private final Runnable onMessage;
    private final byte[] logoutMessage = asciiFix("8=FIX.4.2^9=84^35=5^49=SellSide^" +
                                                  "56=BuySide^34=3^52=20190606-09:25:34.329^" +
                                                  "58=Logout acknowledgement^10=049^");

    private long lastSeenWindowSizeInBytes = -1;

    public RejectLogOn(final Transport transport, final Runnable onMessage)
    {
        this.transport = transport;
        this.onMessage = onMessage;
    }

    private static byte[] asciiFix(final String content)
    {
        return content.replaceAll("\\^", "\u0001").getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof MessageReceived)
        {
            onMessage.run();
        }

        if (event instanceof DataSent)
        {
            DataSent dataSent = (DataSent)event;
            if (lastSeenWindowSizeInBytes != dataSent.windowSizeInBytes())
            {
                System.out.println("Acceptor's updated outbound window size: " + dataSent.windowSizeInBytes());
                lastSeenWindowSizeInBytes = dataSent.windowSizeInBytes();
            }
        }
        if (event instanceof MessageReceived)
        {
            SendMessage sendMessage = transport.command(event, SendMessage.class);
            sendMessage.prepare().putBytes(sendMessage.offset(), logoutMessage);
            sendMessage.commit(logoutMessage.length);
            transport.handle(sendMessage);
        }
    }

    public void work()
    {

    }
}
