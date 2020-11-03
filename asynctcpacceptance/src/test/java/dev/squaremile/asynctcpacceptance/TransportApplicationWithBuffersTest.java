package dev.squaremile.asynctcpacceptance;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.fixtures.ThingsOnDutyRunner;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.testfixtures.EventsSpy;
import dev.squaremile.asynctcp.transport.testfixtures.network.SampleClient;
import dev.squaremile.asynctcpacceptance.sampleapps.MessageEchoApplication;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.transport.testfixtures.Assertions.assertEqual;
import static dev.squaremile.asynctcp.transport.testfixtures.BackgroundRunner.completed;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static dev.squaremile.asynctcp.transport.testfixtures.Worker.runUntil;
import static java.lang.System.arraycopy;

class TransportApplicationWithBuffersTest
{

    private final int port = freePort();
    private final SampleClient sampleClient = new SampleClient();
    private final TransportApplicationFactory transportApplicationFactory = new AsyncTcp().transportAppFactory(NON_PROD_GRADE);
    private final EventsSpy events = EventsSpy.spy();

    @Test
    void shouldAcceptConnectionAndSendDataUsingTcpOverRingBuffer() throws IOException
    {
        ApplicationOnDuty application = transportApplicationFactory.create(
                "test",
                1024 * 1024,
                transport -> new MessageEchoApplication(
                        transport,
                        port,
                        events,
                        fixedLengthDelineation(1),
                        100
                )
        );
        final ThingsOnDutyRunner thingsOnDuty = new ThingsOnDutyRunner(application);

        // Given
        application.onStart();
        runUntil(thingsOnDuty.reached(() -> events.contains(StartedListening.class)));
        assertEqual(events.all(), new StartedListening(port, 100, fixedLengthDelineation(1)));

        // When
        runUntil(thingsOnDuty.reached(completed(() -> sampleClient.connectedTo(port))));
        runUntil(thingsOnDuty.reached(() -> events.all().size() >= 2));

        // Then
        assertThat(events.all(ConnectionAccepted.class).get(0).port()).isEqualTo(port);

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
    }
}
