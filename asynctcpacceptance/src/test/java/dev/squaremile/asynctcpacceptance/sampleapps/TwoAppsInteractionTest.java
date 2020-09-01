package dev.squaremile.asynctcpacceptance.sampleapps;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcp.application.TransportAppLauncher;
import dev.squaremile.asynctcp.application.TransportApplication;
import dev.squaremile.asynctcp.domain.api.events.Connected;
import dev.squaremile.asynctcp.domain.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.domain.api.events.StartedListening;
import dev.squaremile.asynctcp.testfitures.TransportEventsSpy;
import dev.squaremile.asynctcp.testfitures.app.TransportEventsRedirect;

import static dev.squaremile.asynctcp.testfitures.FreePort.freePort;

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
        streamingApplication = new TransportAppLauncher().launch(transport -> new StreamApplication(
                transport,
                host,
                port,
                dataToSend,
                new TransportEventsRedirect(eventsReceivedByStreamingApplication)
        ), "streamingApplication");
        echoApplication = new TransportAppLauncher().launch(transport -> new StreamEchoApplication(
                transport,
                port,
                new TransportEventsRedirect(eventsReceivedByEchoApplication)
        ), "echoApplication");
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
