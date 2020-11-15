package dev.squaremile.fix;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agrona.AsciiSequenceView;


import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.api.wiring.OnEventConnectionApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.fix.certification.FixApplicationFactory;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.fixMessage;

public class FixAcceptorFactory implements ApplicationFactory
{
    private final int port;
    private final Runnable onStartedListening;
    private final OnEventConnectionApplicationFactory onEventConnectionApplicationFactory;

    public FixAcceptorFactory(final int port, final Runnable onStartedListening, final FixApplicationFactory fixApplicationFactory)
    {
        this.port = port;
        this.onStartedListening = onStartedListening;
        this.onEventConnectionApplicationFactory = new UsernameBasedConnectionApplicationFactory(fixApplicationFactory);
    }

    @Override
    public EventDrivenApplication create(final Transport transport)
    {
        return new ListeningApplication(
                transport,
                fixMessage(),
                port,
                event ->
                {
                    if (event instanceof StartedListening)
                    {
                        onStartedListening.run();
                    }
                },
                ConnectionApplicationFactory.onEvent(onEventConnectionApplicationFactory)
        );
    }

    static class UsernameBasedConnectionApplicationFactory implements OnEventConnectionApplicationFactory
    {
        private final FixApplicationFactory fixApplicationFactory;
        private final AsciiSequenceView content = new AsciiSequenceView();
        private final Pattern fixVersionPattern = Pattern.compile("8=(.*?)\u0001");
        private final Pattern usernamePattern = Pattern.compile("\u0001553=(.*?)\u0001");

        UsernameBasedConnectionApplicationFactory(final FixApplicationFactory fixApplicationFactory)
        {
            this.fixApplicationFactory = fixApplicationFactory;
        }

        @Override
        public ConnectionApplication createOnEvent(final ConnectionTransport connectionTransport, final ConnectionEvent event)
        {
            if (event instanceof MessageReceived)
            {
                final String fixMessage = fixMessage((MessageReceived)event);
                if (fixMessage.contains("\u000135=A\u0001"))
                {
                    return fixApplicationFactory.create(connectionTransport, event, fixVersion(fixMessage), username(fixMessage));
                }
            }
            return null;
        }

        private String fixMessage(final MessageReceived message)
        {
            content.wrap(message.buffer(), message.offset(), message.length());
            return content.toString();
        }

        private String username(final String fixMessage)
        {
            return matchedValue(usernamePattern.matcher(fixMessage));
        }

        private String matchedValue(final Matcher matcher)
        {
            return (matcher.find()) ? matcher.group(1) : "";
        }

        private String fixVersion(final String fixMessage)
        {
            return matchedValue(fixVersionPattern.matcher(fixMessage));
        }

    }
}
