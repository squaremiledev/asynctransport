package dev.squaremile.fix;

import java.nio.charset.StandardCharsets;

import org.agrona.AsciiSequenceView;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.DataSent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

public class SendLogOn implements ConnectionApplication
{
    private final Runnable onMessage;
    private final byte[] logonMessage = asciiFix("8=FIXT.1.1^9=116^35=A^49=BuySide^56=SellSide^34=1^" +
                                                 "52=20190605-11:51:27.848^1128=9^98=0^108=30^141=Y^" +
                                                 "553=Username^554=Password^1137=9^10=079^");
    private final int messageCap;
    private final ConnectionTransport transport;
    private ConnectionId connectionId;
    private long lastSeenWindowSizeInBytes = -1;
    private long messagesSent = 0;
    private final AsciiSequenceView content = new AsciiSequenceView();


    public SendLogOn(final ConnectionTransport transport, final Runnable onMessage, final ConnectionId connectionId, final int messageCap)
    {
        this.transport = transport;
        this.onMessage = onMessage;
        this.connectionId = new ConnectionIdValue(connectionId);
        this.messageCap = messageCap;
    }

    private static byte[] asciiFix(final String content)
    {
        return content.replaceAll("\\^", "\u0001").getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public void onStop()
    {
        if (connectionId != null)
        {
            transport.handle(transport.command(CloseConnection.class));
            connectionId = null;
        }
    }

    public void work()
    {
        if (connectionId == null || messagesSent >= messageCap)
        {
            return;
        }
        SendMessage sendMessage = transport.command(SendMessage.class);
        sendMessage.prepare(logonMessage.length).putBytes(sendMessage.offset(), logonMessage);
        sendMessage.commit();
        transport.handle(sendMessage);
        messagesSent++;
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof MessageReceived)
        {
            final MessageReceived message = (MessageReceived)event;
            content.wrap(message.buffer(), message.offset(), message.length());
            for (int i = 0; i < content.length() - 4; i++)
            {
                if (content.charAt(i) == '3' && content.charAt(i + 1) == '5' && content.charAt(i + 2) == '=' && content.charAt(i + 3) == '5')
                {
                    onMessage.run();
                    break;
                }
            }
        }

        if (event instanceof DataSent)
        {
            DataSent dataSent = (DataSent)event;
            if (lastSeenWindowSizeInBytes != dataSent.windowSizeInBytes())
            {
                System.out.println("Initiators's updated outbound window size: " + dataSent.windowSizeInBytes());
                lastSeenWindowSizeInBytes = dataSent.windowSizeInBytes();
            }
        }
    }
}
