package dev.squaremile.asynctcpacceptance.sampleapps;

import java.nio.ByteBuffer;
import java.util.List;

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
import static dev.squaremile.asynctcp.testfitures.StringFixtures.byteArrayWith;
import static dev.squaremile.asynctcp.testfitures.StringFixtures.fixedLengthStringStartingWith;
import static dev.squaremile.asynctcp.testfitures.StringFixtures.stringWith;

class StreamApplicationTest
{
    private final TransportApplication drivingApplication;
    private final TransportApplication transportApplication;
    private final String host = "localhost";
    private final Spin spin;
    private final byte[] dataToSend = byteArrayWith(fixedLengthStringStartingWith("", 100));
    private int port;
    private WhiteboxApplication whiteboxApplication;

    StreamApplicationTest()
    {
        drivingApplication = new TransportAppLauncher().launch(
                transport ->
                {
                    whiteboxApplication = new WhiteboxApplication(transport);
                    return whiteboxApplication;
                }, "");
        drivingApplication.onStart();
        port = freePort();
        transportApplication = new TransportAppLauncher().launch(transport -> new StreamApplication(transport, host, port, dataToSend, IGNORE_EVENTS), "");
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
        assertThat(stringWith(extractedContent(whiteboxApplication.events().all(DataReceived.class))))
                .isEqualTo(stringWith(dataToSend));
    }

    @AfterEach
    void tearDown()
    {
        drivingApplication.onStop();
        transportApplication.onStop();
    }

    private byte[] extractedContent(final List<DataReceived> receivedEvents)
    {
        ByteBuffer actualContent = ByteBuffer.allocate((int)receivedEvents.get(receivedEvents.size() - 1).totalBytesReceived());
        receivedEvents.forEach(event -> event.copyDataTo(actualContent));
        return actualContent.array();
    }
}