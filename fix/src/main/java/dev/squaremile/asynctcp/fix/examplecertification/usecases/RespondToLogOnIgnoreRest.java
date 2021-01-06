package dev.squaremile.asynctcp.fix.examplecertification.usecases;

import org.agrona.AsciiSequenceView;


import dev.squaremile.asynctcp.fix.FixHandler;
import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;

public class RespondToLogOnIgnoreRest implements FixHandler
{
    private final AsciiSequenceView content = new AsciiSequenceView();
    private final byte[] logonMessage;

    public RespondToLogOnIgnoreRest(final String username)
    {
        this.logonMessage = FixUtils.asciiFixBody("FIXT.1.1", "35=A^49=BuySide^56=SellSide^34=1^" +
                                                              "52=20190605-11:51:27.848^1128=9^98=0^108=30^141=Y^" +
                                                              "553=" + username + "^554=Password^1137=9^");
    }

    @Override
    public void onMessage(final ConnectionTransport transport, final MessageReceived messageReceived)
    {
        content.wrap(messageReceived.buffer(), messageReceived.offset(), messageReceived.length());
        for (int i = 0; i < content.length() - 6; i++)
        {
            if (FixUtils.isLogon(content, i))
            {
                final SendMessage sendMessage = transport.command(SendMessage.class);
                sendMessage.prepare().putBytes(sendMessage.offset(), logonMessage);
                sendMessage.commit(logonMessage.length);
                transport.handle(sendMessage);
                break;
            }
        }
    }
}
