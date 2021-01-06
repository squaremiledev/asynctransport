package dev.squaremile.asynctcp;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.fixtures.ResponseApplication;
import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.api.serialization.SerializedMessageListener;
import dev.squaremile.asynctcp.api.transport.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.api.transport.events.ConnectionAccepted;
import dev.squaremile.asynctcp.api.transport.events.StartedListening;
import dev.squaremile.asynctcp.fixtures.transport.EventsSpy;
import dev.squaremile.asynctcp.fixtures.transport.network.SampleClient;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.Assertions.assertEqual;
import static dev.squaremile.asynctcp.fixtures.transport.BackgroundRunner.completed;
import static dev.squaremile.asynctcp.fixtures.transport.FreePort.freePort;
import static dev.squaremile.asynctcp.fixtures.transport.Worker.runUntil;
import static java.lang.System.arraycopy;

class TransportApplicationTest
{
    private final SampleClient sampleClient = new SampleClient();
    private final EventsSpy events = EventsSpy.spy();
    private final int tcpPort = freePort();


    @Test
    void shouldCreateApplicationThatHandlesMessages() throws IOException
    {
        // Define application
        final TransportApplicationOnDutyFactory applicationFactory = transport ->
                new ListeningApplication(
                        transport,
                        fixedLengthDelineation(3),
                        tcpPort,
                        events,
                        (connectionTransport, connectionId) -> new ResponseApplication(connectionTransport, System.out::println, value -> (byte)(value * 10))
                );

        // Create async transport with the application
        final ApplicationOnDuty application = new AsyncTcp().create("echo", 1024 * 1024, SerializedMessageListener.NO_OP, applicationFactory);
        final ThingsOnDutyRunner thingsOnDuty = new ThingsOnDutyRunner(application);

        // When
        application.onStart();

        // Then
        runUntil(thingsOnDuty.reached(() -> events.contains(StartedListening.class)));
        assertEqual(events.all(), new StartedListening(tcpPort, 1, fixedLengthDelineation(3)));

        // When
        runUntil(thingsOnDuty.reached(completed(() -> sampleClient.connectedTo(tcpPort))));
        runUntil(thingsOnDuty.reached(() -> events.all().size() >= 2));

        // Then
        final ConnectionAccepted connectionAccepted = events.all(ConnectionAccepted.class).get(0);
        assertThat(connectionAccepted.port()).isEqualTo(tcpPort);

        // When
        sampleClient.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});

        // Then
        assertThat(readReceivedData(thingsOnDuty, 9)).isEqualTo(new byte[]{10, 20, 30, 40, 50, 60, 70, 80, 90});
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
