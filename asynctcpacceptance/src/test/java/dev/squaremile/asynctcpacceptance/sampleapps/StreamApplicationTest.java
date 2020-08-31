package dev.squaremile.asynctcpacceptance.sampleapps;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.application.TransportAppLauncher;
import dev.squaremile.asynctcp.application.TransportApplication;
import dev.squaremile.asynctcp.domain.api.commands.Listen;
import dev.squaremile.asynctcp.domain.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.domain.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.testfitures.app.WhiteboxApplication;

import static dev.squaremile.asynctcp.domain.api.events.EventListener.IGNORE_EVENTS;
import static dev.squaremile.asynctcp.testfitures.FreePort.freePort;

class StreamApplicationTest
{
    private final TransportApplication drivingApplication;
    private final TransportApplication transportApplication;
    private final String host = "localhost";
    private final Spin spin;
    private final byte[] dataToSend = new byte[100];
    private int port;
    private WhiteboxApplication whiteboxApplication;

    StreamApplicationTest()
    {
        drivingApplication = new TransportAppLauncher().launch(
                transport ->
                {
                    whiteboxApplication = new WhiteboxApplication(transport);
                    return whiteboxApplication;
                });
        drivingApplication.onStart();
        port = freePort();
        transportApplication = new TransportAppLauncher().launch(transport -> new StreamApplication(transport, host, port, dataToSend, IGNORE_EVENTS));
        spin = new Spin(whiteboxApplication, drivingApplication, transportApplication);
        whiteboxApplication.underlyingtTansport().handle(whiteboxApplication.underlyingtTansport().command(Listen.class).set(1, port));
        transportApplication.onStart();
        spin.spinUntil(() -> whiteboxApplication.events().contains(ConnectionAccepted.class));
    }

    @Test
    void shouldConnectUponStart()
    {
        // Expect
        ConnectionAccepted connectionAccepted = whiteboxApplication.events().last(ConnectionAccepted.class);
        assertThat(connectionAccepted.remoteHost()).isEqualTo(host);
        assertThat(connectionAccepted.port()).isEqualTo(port);
    }

    @Test
    void shouldDisconnectWhenStopped()
    {
        // When
        transportApplication.onStop();

        // Then
        spin.spinUntil(() -> whiteboxApplication.events().contains(ConnectionClosed.class));
    }

    @Test
    void shouldSendDataWhenConnected()
    {
        spin.spinUntil(() -> whiteboxApplication.events().contains(ConnectionAccepted.class));
        spin.spinUntil(() -> whiteboxApplication.events().contains(DataReceived.class) && whiteboxApplication.events().last(DataReceived.class).totalBytesReceived() == dataToSend.length);
    }

    @AfterEach
    void tearDown()
    {
        drivingApplication.onStop();
        transportApplication.onStop();
    }
}