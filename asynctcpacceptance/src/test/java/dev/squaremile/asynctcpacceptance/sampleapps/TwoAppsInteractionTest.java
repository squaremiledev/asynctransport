package dev.squaremile.asynctcpacceptance.sampleapps;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.values.PredefinedTransportEncoding;
import dev.squaremile.asynctcp.transport.setup.TransportAppFactory;
import dev.squaremile.asynctcp.transport.setup.TransportApplication;
import dev.squaremile.asynctcp.transport.testfixtures.TransportEventsSpy;
import dev.squaremile.asynctcp.transport.testfixtures.app.TransportEventsRedirect;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

public class TwoAppsInteractionTest
{
    private final TransportApplication streamingApplication;
    private final TransportApplication echoApplication;
    private final TransportEventsSpy eventsReceivedByStreamingApplication = new TransportEventsSpy();
    private final TransportEventsSpy eventsReceivedByEchoApplication = new TransportEventsSpy();
    private final String host = "localhost";
    private final byte[] dataToSend = new byte[100];
    private final Spin spin;
    private int port;

    TwoAppsInteractionTest()
    {
        port = freePort();
        streamingApplication = new TransportAppFactory().create("streamingApplication", transport -> new ByteMessageSendingApplication(
                transport,
                host,
                port,
                dataToSend,
                new TransportEventsRedirect(eventsReceivedByStreamingApplication)
        ));
        echoApplication = new TransportAppFactory().create("echoApplication", transport -> new EchoApplication(
                transport,
                port,
                new TransportEventsRedirect(eventsReceivedByEchoApplication), PredefinedTransportEncoding.SINGLE_BYTE
        ));
        spin = new Spin(streamingApplication, echoApplication);
    }

    @Test
    void shouldConnect()
    {
        // When
        echoApplication.onStart();

        // Ten
        spin.spinUntil(() -> eventsReceivedByEchoApplication.contains(StartedListening.class));

        // When
        streamingApplication.onStart();

        // Then
        spin.spinUntil(() -> eventsReceivedByEchoApplication.contains(ConnectionAccepted.class));
        spin.spinUntil(() -> eventsReceivedByStreamingApplication.contains(Connected.class));
    }

    @AfterEach
    void tearDown()
    {
        echoApplication.onStop();
        streamingApplication.onStop();
    }
}
