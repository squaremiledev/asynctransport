package dev.squaremile.asynctcp.fix.examplecertification.usecases;

import org.agrona.AsciiSequenceView;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

public class RespondToLogOnIgnoreRest implements ConnectionApplication
{
    private final ConnectionTransport transport;
    private final AsciiSequenceView content = new AsciiSequenceView();
    private final byte[] logonMessage;

    public RespondToLogOnIgnoreRest(final ConnectionTransport transport)
    {
        this.transport = transport;
        this.logonMessage = FixUtils.asciiFixBody("FIXT.1.1", "35=A^49=BuySide^56=SellSide^34=1^" +
                                                              "52=20190605-11:51:27.848^1128=9^98=0^108=30^141=Y^" +
                                                              "553=" + "Username" + "^554=Password^1137=9^");
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof MessageReceived)
        {
            final MessageReceived messageReceived = (MessageReceived)event;
            content.wrap(messageReceived.buffer(), messageReceived.offset(), messageReceived.length());
            for (int i = 0; i < content.length() - 6; i++)
            {
                if (FixUtils.isLogon(content, i))
                {
                    final SendMessage sendMessage = transport.command(SendMessage.class);
                    sendMessage.prepare(logonMessage.length).putBytes(sendMessage.offset(), logonMessage);
                    sendMessage.commit();
                    transport.handle(sendMessage);
                    break;
                }
            }
        }
    }

    @Override
    public void work()
    {

    }

}
