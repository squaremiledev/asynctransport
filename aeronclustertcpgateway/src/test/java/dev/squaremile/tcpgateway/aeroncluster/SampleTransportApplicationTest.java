package dev.squaremile.tcpgateway.aeroncluster;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.fixtures.ResponseApplication;
import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.fixtures.TimingExtension;
import dev.squaremile.asynctcp.serialization.api.SerializedMessageListener;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.values.Delineation;
import dev.squaremile.asynctcp.transport.testfixtures.EventsSpy;
import dev.squaremile.asynctcp.transport.testfixtures.network.SampleClient;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.transport.api.app.EventListener.IGNORE_EVENTS;
import static dev.squaremile.asynctcp.transport.testfixtures.Assertions.assertEqual;
import static dev.squaremile.asynctcp.transport.testfixtures.BackgroundRunner.completed;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.runUntil;
import static java.lang.System.arraycopy;

@ExtendWith(TimingExtension.class)
class SampleTransportApplicationTest
{

    private static final Delineation DELINEATION = fixedLengthDelineation(3);

    private final int port = freePort();
    private final SampleClient sampleClient = new SampleClient();
    private final TransportApplicationFactory transportApplicationFactory = new AsyncTcp();
    private final EventsSpy events = EventsSpy.spy();


    @Test
    void shouldCreateApplicationThatHandlesMessages() throws IOException
    {
        final ApplicationOnDuty application = transportApplicationFactory.create(
                "echo",
                1024 * 1024,
                SerializedMessageListener.NO_OP,
                transport ->
                        new ListeningApplication(
                                transport,
                                DELINEATION,
                                port,
                                events,
                                (connectionTransport, connectionId) -> new ResponseApplication(
                                        connectionTransport, IGNORE_EVENTS, value -> (byte)(value * 10)
                                )
                        )
        );
        final ThingsOnDutyRunner thingsOnDuty = new ThingsOnDutyRunner(application);

        // Given
        application.onStart();
        runUntil(thingsOnDuty.reached(() -> events.contains(StartedListening.class)));
        assertEqual(events.all(), new StartedListening(port, 1, fixedLengthDelineation(3)));

        // When
        runUntil(thingsOnDuty.reached(completed(() -> sampleClient.connectedTo(port))));
        runUntil(thingsOnDuty.reached(() -> events.all().size() >= 2));

        // Then
        final ConnectionAccepted connectionAccepted = events.all(ConnectionAccepted.class).get(0);
        assertThat(connectionAccepted.port()).isEqualTo(port);

        // DATA SENDING PART
        byte[] dataToSend = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        byte[] expectedReceivedData = new byte[]{10, 20, 30, 40, 50, 60, 70, 80, 90};

        // When
        sampleClient.write(dataToSend);

        // Then
        assertThat(readReceivedData(thingsOnDuty, dataToSend.length)).isEqualTo(expectedReceivedData);
    }

    private byte[] readReceivedData(final ThingsOnDutyRunner thingsOnDuty, final int expectedDataLength)
    {
        byte[] actualReceivedData = new byte[expectedDataLength];
        runUntil(thingsOnDuty.reached(completed(() -> sampleClient.read(
                actualReceivedData.length,
                actualReceivedData.length,
                (data, length) -> arraycopy(data, 0, actualReceivedData, 0, length)
        ))));
        return actualReceivedData;
    }
}
