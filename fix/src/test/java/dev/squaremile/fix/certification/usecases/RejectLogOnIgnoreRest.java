package dev.squaremile.fix.certification.usecases;

import org.agrona.AsciiSequenceView;


import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.fix.certification.FakeApplication;
import dev.squaremile.fix.utils.FixUtils;

import static dev.squaremile.fix.utils.FixUtils.asciiFixBody;

public class RejectLogOnIgnoreRest implements FakeApplication
{
    private final ConnectionTransport transport;
    private final AsciiSequenceView content = new AsciiSequenceView();
    private final byte[] logoutMessage = asciiFixBody("FIX.4.2", "35=5^49=SellSide^" +
                                                                 "56=BuySide^34=3^52=20190606-09:25:34.329^" +
                                                                 "58=Logout acknowledgement^");

    public RejectLogOnIgnoreRest(final ConnectionTransport transport)
    {
        this.transport = transport;
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
                    sendMessage.prepare(logoutMessage.length).putBytes(sendMessage.offset(), logoutMessage);
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
