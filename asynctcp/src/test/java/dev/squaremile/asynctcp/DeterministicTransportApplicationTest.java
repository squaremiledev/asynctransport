package dev.squaremile.asynctcp;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.api.transport.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.api.transport.events.ConnectionAccepted;
import dev.squaremile.asynctcp.api.transport.events.StartedListening;
import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.fixtures.MessageLog;
import dev.squaremile.asynctcp.fixtures.ResponseApplication;
import dev.squaremile.asynctcp.support.transport.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.fixtures.transport.EventsSpy;
import dev.squaremile.asynctcp.fixtures.transport.network.SampleClient;

import static dev.squaremile.asynctcp.Assertions.assertEqual;
import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.api.transport.app.EventListener.IGNORE_EVENTS;
import static dev.squaremile.asynctcp.fixtures.transport.BackgroundRunner.completed;
import static dev.squaremile.asynctcp.support.transport.FreePort.freePort;
import static dev.squaremile.asynctcp.support.transport.Worker.runUntil;
import static java.lang.System.arraycopy;

class DeterministicTransportApplicationTest
{

    private final int port = freePort();
    private final SampleClient sampleClient = new SampleClient();
    private final TransportApplicationFactory transportApplicationFactory = new AsyncTcp();
    private final EventsSpy events = EventsSpy.spy();

    static Stream<Arguments> connectionFactories()
    {
        final ConnectionApplicationFactory onCreateFactory = ConnectionApplicationFactory.onCreate(
                (connectionTransport, connectionId) -> new ResponseApplication(connectionTransport, IGNORE_EVENTS, value -> value)
        );
        final ConnectionApplicationFactory onStart = ConnectionApplicationFactory.onStart(
                (connectionTransport, connectionId) -> new ResponseApplication(connectionTransport, IGNORE_EVENTS, value -> value)
        );
        final ConnectionApplicationFactory onEventFactory = ConnectionApplicationFactory.onEvent(
                (connectionTransport, event) -> Optional.of(new ResponseApplication(connectionTransport, IGNORE_EVENTS, value -> value))
        );

        return Stream.of(
                arguments(onCreateFactory, onCreateFactory),
                arguments(onCreateFactory, onStart),
                arguments(onCreateFactory, onEventFactory),
                arguments(onStart, onCreateFactory),
                arguments(onStart, onStart),
                arguments(onStart, onEventFactory),
                arguments(onEventFactory, onCreateFactory),
                arguments(onEventFactory, onStart),
                arguments(onEventFactory, onEventFactory)
        );
    }

    @ParameterizedTest
    @MethodSource("connectionFactories")
    void shouldSupportDeterministicApplication(final ConnectionApplicationFactory originalConnectionAppFactory, final ConnectionApplicationFactory replayedConnectionAppFactory) throws IOException
    {
        final MessageLog messageLog = new MessageLog();
        ApplicationOnDuty application = transportApplicationFactory.create(
                "echo",
                1024 * 1024,
                messageLog,
                transport ->
                        new ListeningApplication(
                                transport,
                                fixedLengthDelineation(1),
                                port,
                                events,
                                originalConnectionAppFactory
                        )
        );
        final ThingsOnDutyRunner thingsOnDuty = new ThingsOnDutyRunner(application);

        // Given
        application.onStart();
        runUntil(thingsOnDuty.reached(() -> events.contains(StartedListening.class)));
        assertEqual(events.all(), new StartedListening(port, 1, fixedLengthDelineation(1)));

        // When
        runUntil(thingsOnDuty.reached(completed(() -> sampleClient.connectedTo(port))));
        runUntil(thingsOnDuty.reached(() -> events.all().size() >= 2));

        // Then
        final ConnectionAccepted connectionAccepted = events.all(ConnectionAccepted.class).get(0);
        assertThat(connectionAccepted.port()).isEqualTo(port);

        // DATA SENDING PART

        byte[] contentSent = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        byte[] contentReceived = new byte[]{99, 99, 99, 99, 99, 99, 99, 99};

        // When
        sampleClient.write(contentSent);
        runUntil(thingsOnDuty.reached(completed(() -> sampleClient.read(
                contentSent.length,
                contentSent.length,
                (data, length) -> arraycopy(data, 0, contentReceived, 0, length)
        ))));

        // Then
        assertThat(contentReceived).isEqualTo(contentSent);
        verifyMessageLog(messageLog);
        verifyDeterminism(
                messageLog,
                transport ->
                        new ListeningApplication(
                                transport,
                                fixedLengthDelineation(1),
                                port,
                                events,
                                replayedConnectionAppFactory
                        )
        );
    }

    private void verifyMessageLog(final MessageLog messageLog)
    {
        String logContent = messageLog.logContent();
        assertThat(logContent).hasSizeBetween(2000, 10000);
        // to check that it can be read again
        assertThat(messageLog.logContent()).isEqualTo(logContent);
    }

    private void verifyDeterminism(final MessageLog previousMessageLog, final TransportApplicationOnDutyFactory applicationFactory)
    {
        final MessageLog newMessageLog = new MessageLog();
        ApplicationOnDuty newApplication = transportApplicationFactory.createWithoutTransport(
                "new",
                applicationFactory,
                previousMessageLog.createCapturedEventsSupplier(),
                newMessageLog::onSerialized,
                newMessageLog::onSerialized
        );

        // When
        newApplication.onStart();
        runUntil(new ThingsOnDutyRunner(newApplication).reached(() -> newMessageLog.logContent().length() >= previousMessageLog.logContent().length()));

        // Then
        assertThat(newMessageLog.logContent()).isEqualTo(previousMessageLog.logContent());
    }
}
