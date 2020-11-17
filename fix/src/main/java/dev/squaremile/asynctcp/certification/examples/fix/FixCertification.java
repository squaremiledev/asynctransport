package dev.squaremile.asynctcp.certification.examples.fix;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agrona.AsciiSequenceView;


import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.certification.Certification;
import dev.squaremile.asynctcp.certification.IgnoreAll;
import dev.squaremile.asynctcp.certification.examples.fix.usecases.RejectLogOnIgnoreRest;
import dev.squaremile.asynctcp.certification.examples.fix.usecases.RespondToLogOnIgnoreRest;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.fixMessage;

public class FixCertification
{
    public static final UseCase USE_CASE_001_ACCEPTED_LOGON = new UseCase("FIX.4.2", "useCase001", (connectionTransport, connectionId) -> new RespondToLogOnIgnoreRest(connectionTransport));
    public static final UseCase USE_CASE_002_REJECTED_LOGON = new UseCase("FIXT.1.1", "useCase002", (connectionTransport, connectionId) -> new RejectLogOnIgnoreRest(connectionTransport));
    public static final UseCase USE_CASE_002_NOT_RESPONDING = new UseCase("FIXT.1.1", "useCase003", (connectionTransport, connectionId) -> new IgnoreAll());

    public static Certification<UseCase> fixCertification()
    {
        return new Certification<>(
                1024 * 1024,
                fixMessage(),
                new Resolver(
                        USE_CASE_001_ACCEPTED_LOGON,
                        USE_CASE_002_REJECTED_LOGON,
                        USE_CASE_002_NOT_RESPONDING
                )
        );
    }

    public static class UseCase implements dev.squaremile.asynctcp.certification.UseCase
    {
        private final String fixVersion;
        private final String username;
        private final ConnectionApplicationFactory connectionApplicationFactory;

        public UseCase(
                final String fixVersion,
                final String username,
                final ConnectionApplicationFactory connectionApplicationFactory
        )
        {
            this.username = username;
            this.fixVersion = fixVersion;
            this.connectionApplicationFactory = connectionApplicationFactory;
        }

        @Override
        public ConnectionApplicationFactory fakeAppFactory()
        {
            return connectionApplicationFactory;
        }

        public String username()
        {
            return username;
        }

        public String fixVersion()
        {
            return fixVersion;
        }
    }

    public static class Resolver implements dev.squaremile.asynctcp.certification.Resolver<UseCase>
    {
        private final AsciiSequenceView content = new AsciiSequenceView();
        private final Pattern fixVersionPattern = Pattern.compile("8=(.*?)\u0001");
        private final Pattern usernamePattern = Pattern.compile("\u0001553=(.*?)\u0001");
        private final List<UseCase> useCases;


        public Resolver(UseCase... useCases)
        {
            this.useCases = Arrays.asList(useCases);
        }

        @Override
        public Optional<UseCase> useCase(final MessageReceived messageReceived)
        {
            final String fixMessage = fixMessage(messageReceived);
            if (fixMessage.contains("\u000135=A\u0001"))
            {
                final String fixVersion = fixVersion(fixMessage);
                final String username = username(fixMessage);
                for (final UseCase useCase : useCases)
                {
                    if (useCase.fixVersion().equals(fixVersion) && useCase.username().equals(username))
                    {
                        return Optional.of(useCase);
                    }
                }
                throw new IllegalArgumentException("No use case found for " + fixMessage);
            }
            return Optional.empty();
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
