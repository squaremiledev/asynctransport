package dev.squaremile.asynctcp.fix.examplecertification;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agrona.AsciiSequenceView;


import dev.squaremile.asynctcp.api.certification.Certification;
import dev.squaremile.asynctcp.api.certification.IgnoreAll;
import dev.squaremile.asynctcp.api.certification.UseCase;
import dev.squaremile.asynctcp.api.certification.UseCases;
import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.fix.examplecertification.usecases.RejectLogOnIgnoreRest;
import dev.squaremile.asynctcp.fix.examplecertification.usecases.RespondToLogOnIgnoreRest;
import dev.squaremile.asynctcp.api.transport.app.ConnectionApplication;
import dev.squaremile.asynctcp.api.transport.app.ConnectionEvent;
import dev.squaremile.asynctcp.api.transport.app.ConnectionTransport;
import dev.squaremile.asynctcp.api.transport.events.MessageReceived;
import dev.squaremile.asynctcp.api.transport.values.ConnectionId;

import static dev.squaremile.asynctcp.fix.FixHandlerFactory.createFixHandler;
import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.fixMessage;

public class FixCertification
{
    public static final FixUseCase USE_CASE_001_ACCEPTED_LOGON = new FixUseCase("FIX.4.2", "UCAcceptLogon", createFixHandler(new RespondToLogOnIgnoreRest("Username")));
    public static final FixUseCase USE_CASE_002_FIX11_REJECTED_LOGON = new FixUseCase(
            "FIXT.1.1",
            "UCRejectLogon",
            createFixHandler(new RejectLogOnIgnoreRest())
    );
    public static final FixUseCase USE_CASE_002_FIX42_REJECTED_LOGON = new FixUseCase(
            "FIX.4.2",
            "UCRejectLogon",
            createFixHandler(new RejectLogOnIgnoreRest())
    );
    public static final FixUseCase USE_CASE_002_NOT_RESPONDING = new FixUseCase("FIXT.1.1", "UCIgnoreAll", (connectionTransport, connectionId) -> new IgnoreAll());

    public static Certification fixCertification()
    {
        return new Certification(
                1024 * 1024,
                fixMessage(),
                new UseCasesPerFixVersionAndUsername(
                        USE_CASE_001_ACCEPTED_LOGON,
                        USE_CASE_002_FIX11_REJECTED_LOGON,
                        USE_CASE_002_FIX42_REJECTED_LOGON,
                        USE_CASE_002_NOT_RESPONDING
                )
        );
    }

    public static class FixUseCase implements UseCase
    {
        private final String fixVersion;
        private final String username;
        private final ConnectionApplicationFactory connectionApplicationFactory;

        public FixUseCase(
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

    private static class SessionLayerConnectionApplication implements ConnectionApplication
    {
        private final ConnectionApplication application;

        public SessionLayerConnectionApplication(final FixUseCase useCase, final ConnectionTransport connectionTransport, final ConnectionId connectionId)
        {
            application = useCase.fakeAppFactory().create(connectionTransport, connectionId);
        }

        @Override
        public void onEvent(final ConnectionEvent event)
        {
            application.onEvent(event);
        }
    }

    public static class UseCasesPerFixVersionAndUsername implements UseCases
    {
        private final AsciiSequenceView content = new AsciiSequenceView();
        private final Pattern fixVersionPattern = Pattern.compile("8=(.*?)\u0001");
        private final Pattern usernamePattern = Pattern.compile("\u0001553=(.*?)\u0001");
        private final List<FixUseCase> useCases;


        public UseCasesPerFixVersionAndUsername(FixUseCase... useCases)
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
                return useCases.stream()
                        .filter(useCase -> useCase.fixVersion().equals(fixVersion) && useCase.username().equals(username))
                        .map(useCase -> Optional.of((UseCase)() -> (connectionTransport, connectionId) -> new SessionLayerConnectionApplication(useCase, connectionTransport, connectionId)))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No use case found for " + fixMessage));
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
