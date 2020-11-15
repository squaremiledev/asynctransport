package dev.squaremile.asynctcpacceptance.sampleapps;

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
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.fixMessage;

class FixAcceptorFactory implements ApplicationFactory
{
    private final int port;
    private final Runnable onStartedListening;
    private final OnEventConnectionApplicationFactory onEventConnectionApplicationFactory;

    FixAcceptorFactory(final int port, final Runnable onStartedListening, final ApplicationForUser applicationForUser)
    {
        this.port = port;
        this.onStartedListening = onStartedListening;
        this.onEventConnectionApplicationFactory = new UsernameBasedConnectionApplicationFactory(applicationForUser);
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

    interface ApplicationForUser
    {
        ConnectionApplication create(final ConnectionTransport connectionTransport, final ConnectionId connectionId, final String fixVersion, final String username);
    }

    static class UsernameBasedConnectionApplicationFactory implements OnEventConnectionApplicationFactory
    {
        private final FixAcceptorFactory.ApplicationForUser applicationForUser;
        private final AsciiSequenceView content = new AsciiSequenceView();
        private final Pattern fixVersionPattern = Pattern.compile("8=(.*?)\u0001");
        private final Pattern usernamePattern = Pattern.compile("\u0001553=(.*?)\u0001");

        UsernameBasedConnectionApplicationFactory(final FixAcceptorFactory.ApplicationForUser applicationForUser)
        {
            this.applicationForUser = applicationForUser;
        }

        @Override
        public ConnectionApplication createOnEvent(final ConnectionTransport connectionTransport, final ConnectionEvent event)
        {
            if (event instanceof MessageReceived)
            {
                final String fixMessage = fixMessage((MessageReceived)event);
                if (fixMessage.contains("\u000135=A\u0001"))
                {
                    return applicationForUser.create(connectionTransport, event, fixVersion(fixMessage), username(fixMessage));
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
