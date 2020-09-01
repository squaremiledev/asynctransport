package dev.squaremile.asynctcpacceptance.sampleapps;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.application.TransportAppLauncher;
import dev.squaremile.asynctcp.application.TransportApplication;
import dev.squaremile.asynctcp.domain.api.StandardEncoding;
import dev.squaremile.asynctcp.domain.api.commands.Connect;
import dev.squaremile.asynctcp.domain.api.commands.SendData;
import dev.squaremile.asynctcp.domain.api.events.CommandFailed;
import dev.squaremile.asynctcp.domain.api.events.Connected;
import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.testfitures.TransportEventsSpy;
import dev.squaremile.asynctcp.testfitures.app.WhiteboxApplication;

import static dev.squaremile.asynctcp.domain.api.events.EventListener.IGNORE_EVENTS;
import static dev.squaremile.asynctcp.testfitures.FreePort.freePort;

class EchoApplicationTest
{
    private final TransportApplication drivingApplication;
    private final TransportApplication transportApplication;
    private final Spin spin;
    private int port;
    private WhiteboxApplication<TransportEventsSpy> whiteboxApplication;

    EchoApplicationTest()
    {
        drivingApplication = new TransportAppLauncher().launch(
                transport ->
                {
                    whiteboxApplication = new WhiteboxApplication<>(transport, new TransportEventsSpy());
                    return whiteboxApplication;
                }, "");
        drivingApplication.onStart();
        port = freePort();
        transportApplication = new TransportAppLauncher().launch(transport -> new EchoApplication(transport, port, IGNORE_EVENTS, StandardEncoding.LONGS), "");
        spin = new Spin(whiteboxApplication, drivingApplication, transportApplication);
        transportApplication.onStart();
        transportApplication.work();
    }

    private static ByteBuffer bufferWithMonotonicallyIncreasingLongs(final int howMany)
    {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[howMany * 8]);
        for (int i = 0; i < howMany; i++)
        {
            buffer.putLong(i);
        }
        return buffer;
    }

    @Test
    void shouldListenUponStartAndStopListeningWhenStopped()
    {
        // When
        whiteboxApplication.underlyingtTansport().handle(whiteboxApplication.underlyingtTansport().command(Connect.class).set("localhost", port, 1, 50));
        spin.spinUntil(() -> whiteboxApplication.events().contains(Connected.class));

        // Then
        assertThat(whiteboxApplication.events().all(Connected.class)).hasSize(1);

        // When
        transportApplication.onStop();
        transportApplication.work();
        whiteboxApplication.underlyingtTansport().handle(whiteboxApplication.underlyingtTansport().command(Connect.class).set("localhost", port, 2, 50));
        spin.spinUntilAllowingFailures(() -> whiteboxApplication.events().contains(CommandFailed.class));

        // Then
        assertThat(whiteboxApplication.events().last(CommandFailed.class).commandId()).isEqualTo(2);
    }

    @Test
    void shouldEchoBackTheStream()
    {
        ByteBuffer content = bufferWithMonotonicallyIncreasingLongs(10);
        Connected connected = connect();
        assertThat(connected).isNotNull();

        whiteboxApplication.underlyingtTansport().handle(whiteboxApplication.underlyingtTansport().command(connected, SendData.class).set(content.array(), 101));
        spin.spinUntil(() -> whiteboxApplication.events().contains(DataReceived.class) && whiteboxApplication.events().last(DataReceived.class).totalBytesReceived() == content.array().length);
        assertThat(extractedContent(whiteboxApplication.events().all(DataReceived.class))).isEqualTo(content.array());
    }

    @AfterEach
    void tearDown()
    {
        drivingApplication.onStop();
        drivingApplication.work();
        transportApplication.onStop();
        transportApplication.work();
    }

    private Connected connect()
    {
        whiteboxApplication.underlyingtTansport().handle(whiteboxApplication.underlyingtTansport().command(Connect.class).set("localhost", port, 1, 50));
        whiteboxApplication.underlyingtTansport().work();
        spin.spinUntil(() -> whiteboxApplication.events().contains(Connected.class));
        return whiteboxApplication.events().lastResponse(Connected.class, 1);
    }

    private byte[] extractedContent(final List<DataReceived> receivedEvents)
    {
        ByteBuffer actualContent = ByteBuffer.allocate((int)receivedEvents.get(receivedEvents.size() - 1).totalBytesReceived());
        receivedEvents.forEach(event -> event.copyDataTo(actualContent));
        return actualContent.array();
    }
}