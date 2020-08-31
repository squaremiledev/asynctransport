package dev.squaremile.asynctcpacceptance.sampleapps;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcp.application.TransportAppLauncher;
import dev.squaremile.asynctcp.application.TransportApplication;
import dev.squaremile.asynctcp.domain.api.events.Connected;
import dev.squaremile.asynctcp.domain.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.domain.api.events.StartedListening;
import dev.squaremile.asynctcp.testfitures.TransportEventsSpy;
import dev.squaremile.asynctcp.testfitures.app.TransportEventsRedirect;

import static dev.squaremile.asynctcp.testfitures.FreePort.freePort;

public class RetryingStreamingApplicationTest
{
    private final TransportApplication streamingApplication;
    private final TransportApplication echoApplication;
    private final TransportEventsSpy eventsReceivedByStreamingApplication = new TransportEventsSpy();
    private final TransportEventsSpy eventsReceivedByEchoApplication = new TransportEventsSpy();
    private final String host = "localhost";
    private final byte[] dataToSend = new byte[100];
    private final Spin spin;
    private int port;

    RetryingStreamingApplicationTest()
    {
        port = freePort();
        streamingApplication = new TransportAppLauncher().launch(transport -> new StreamApplication(
                transport,
                host,
                port,
                dataToSend,
                new TransportEventsRedirect(eventsReceivedByStreamingApplication)
        ));
        echoApplication = new TransportAppLauncher().launch(transport -> new StreamEchoApplication(
                transport,
                port,
                new TransportEventsRedirect(eventsReceivedByEchoApplication)
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

    @Test
    @Disabled
    void shouldKeepTryingToConnect()
    {
        streamingApplication.onStart();
        echoApplication.onStart();
        spin.spinUntil(() ->
                       {
//                           System.out.println("eventsReceivedByStreamingApplication = " + eventsReceivedByStreamingApplication.all());
//                           System.out.println("eventsReceivedByEchoApplication = " + eventsReceivedByEchoApplication.all());
                           return eventsReceivedByEchoApplication.contains(Connected.class);
                       });
    }

    @AfterEach
    void tearDown()
    {
        echoApplication.onStop();
        streamingApplication.onStop();
    }
}
