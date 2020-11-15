package dev.squaremile.fix;

import java.nio.charset.StandardCharsets;

import org.agrona.AsciiSequenceView;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.DataSent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

public class RejectLogOn implements ConnectionApplication
{
    private final Runnable onMessage;
    private final ConnectionTransport transport;
    private final AsciiSequenceView content = new AsciiSequenceView();
    private final byte[] logoutMessage = asciiFix("8=FIX.4.2^9=84^35=5^49=SellSide^" +
                                                  "56=BuySide^34=3^52=20190606-09:25:34.329^" +
                                                  "58=Logout acknowledgement^10=049^");

    private long lastSeenWindowSizeInBytes = -1;

    public RejectLogOn(final ConnectionTransport transport, final Runnable onMessage)
    {
        this.transport = transport;
        this.onMessage = onMessage;
    }

    private static byte[] asciiFix(final String content)
    {
        return content.replaceAll("\\^", "\u0001").getBytes(StandardCharsets.US_ASCII);
    }

    public void work()
    {

    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof DataSent)
        {
            DataSent dataSent = (DataSent)event;
            if (lastSeenWindowSizeInBytes != dataSent.windowSizeInBytes())
            {
                System.out.println("Acceptor's updated outbound window size: " + dataSent.windowSizeInBytes());
                lastSeenWindowSizeInBytes = dataSent.windowSizeInBytes();
            }
        }
        else if (event instanceof MessageReceived)
        {
            final MessageReceived messageReceived = (MessageReceived)event;
            content.wrap(messageReceived.buffer(), messageReceived.offset(), messageReceived.length());
            for (int i = 0; i < content.length() - 4; i++)
            {
                if (content.charAt(i) == '3' && content.charAt(i + 1) == '5' && content.charAt(i + 2) == '=' && content.charAt(i + 3) == 'A')
                {
                    onMessage.run();
                    final SendMessage sendMessage = transport.command(SendMessage.class);
                    sendMessage.prepare(logoutMessage.length).putBytes(sendMessage.offset(), logoutMessage);
                    sendMessage.commit();
                    transport.handle(sendMessage);
                    break;
                }
            }
        }
    }
}
