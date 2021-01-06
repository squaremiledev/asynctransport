package dev.squaremile.asynctcp.fix.examplecertification.usecases;

import org.agrona.AsciiSequenceView;


import dev.squaremile.asynctcp.fix.FixHandler;
import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;

import static dev.squaremile.asynctcp.fix.examplecertification.usecases.FixUtils.asciiFixBody;


public class RejectLogOnIgnoreRest implements FixHandler
{
    private final AsciiSequenceView content = new AsciiSequenceView();
    private final byte[] logoutMessage = asciiFixBody("FIX.4.2", "35=5^49=SellSide^" +
                                                                 "56=BuySide^34=3^52=20190606-09:25:34.329^" +
                                                                 "58=Logout acknowledgement^");

    @Override
    public void onMessage(final ConnectionTransport transport, final MessageReceived messageReceived)
    {
        content.wrap(messageReceived.buffer(), messageReceived.offset(), messageReceived.length());
        for (int i = 0; i < content.length() - 6; i++)
        {
            if (FixUtils.isLogon(content, i))
            {
                final SendMessage sendMessage = transport.command(SendMessage.class);
                sendMessage.prepare().putBytes(sendMessage.offset(), logoutMessage);
                sendMessage.commit(logoutMessage.length);
                transport.handle(sendMessage);
                break;
            }
        }
    }
}
