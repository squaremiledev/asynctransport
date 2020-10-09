package dev.squaremile.asynctcpacceptance.sampleapps.fix;

import java.nio.charset.StandardCharsets;


import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.DataSent;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionEventsListener;

public class Initiator implements ConnectionEventsListener
{
    private final Transport transport;
    private final byte[] logonMessage = asciiFix("8=FIXT.1.1^9=116^35=A^49=BuySide^56=SellSide^34=1^" +
                                                 "52=20190605-11:51:27.848^1128=9^98=0^108=30^141=Y^" +
                                                 "553=Username^554=Password^1137=9^10=079^");
    private ConnectionId connectionId;
    private long lastSeenWindowSizeInBytes = -1;


    public Initiator(final Transport transport)
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

    public void disconnect()
    {
        if (connectionId != null)
        {
            ConnectionId connectionToClose = this.connectionId;
            connectionId = null;
            transport.handle(transport.command(connectionToClose, CloseConnection.class));
        }
    }

    public void onConnected(final Connected connected)
    {
        this.connectionId = connected.copy();
    }

    public void work()
    {
        if (connectionId == null)
        {
            return;
        }
        SendMessage sendMessage = transport.command(connectionId, SendMessage.class);
        sendMessage.prepare().putBytes(sendMessage.offset(), logonMessage);
        sendMessage.commit(logonMessage.length);
        transport.handle(sendMessage);
    }
}
