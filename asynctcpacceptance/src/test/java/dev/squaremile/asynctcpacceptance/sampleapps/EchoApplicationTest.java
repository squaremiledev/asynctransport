package dev.squaremile.asynctcpacceptance.sampleapps;

import java.nio.ByteBuffer;
import java.util.List;

import org.agrona.collections.MutableInteger;
import org.agrona.collections.MutableReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.transport.api.app.CommandFailed;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.setup.TransportAppFactory;
import dev.squaremile.asynctcp.transport.setup.TransportApplication;
import dev.squaremile.asynctcp.transport.testfixtures.TransportEventsSpy;
import dev.squaremile.asynctcp.transport.testfixtures.app.Pier;
import dev.squaremile.asynctcp.transport.testfixtures.app.WhiteboxApplication;

import static dev.squaremile.asynctcp.serialization.api.delineation.PredefinedTransportDelineation.RAW_STREAMING;
import static dev.squaremile.asynctcp.transport.api.app.EventListener.IGNORE_EVENTS;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

class EchoApplicationTest
{
    private final TransportApplication drivingApplication;
    private final TransportApplication transportApplicationUnderTest;
    private final Spin spin;
    private final Pier pier;
    private final int port = freePort();

    EchoApplicationTest()
    {
        final MutableReference<WhiteboxApplication<TransportEventsSpy>> whiteboxApplication = new MutableReference<>();
        drivingApplication = new TransportAppFactory().create(
                "", transport ->
                {
                    whiteboxApplication.set(new WhiteboxApplication<>(transport, new TransportEventsSpy()));
                    return whiteboxApplication.get();
                });
        drivingApplication.onStart();
        transportApplicationUnderTest = new TransportAppFactory().create("", transport -> new EchoApplication(transport, port, IGNORE_EVENTS, 101));
        pier = new Pier(whiteboxApplication.get().underlyingTransport(), whiteboxApplication.get().events());
        spin = new Spin(whiteboxApplication.get(), drivingApplication, transportApplicationUnderTest);
        transportApplicationUnderTest.onStart();
        transportApplicationUnderTest.work();
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
        pier.handle(pier.command(Connect.class).set("localhost", port, (long)1, 50, RAW_STREAMING.type));
        spin.spinUntil(() -> pier.receivedEvents().contains(Connected.class));

        // Then
        assertThat(pier.receivedEvents().all(Connected.class)).hasSize(1);

        // When
        transportApplicationUnderTest.onStop();
        transportApplicationUnderTest.work();
        pier.handle(pier.command(Connect.class).set("localhost", port, (long)2, 50, RAW_STREAMING.type));
        spin.spinUntilAllowingFailures(() -> pier.receivedEvents().contains(CommandFailed.class));

        // Then
        assertThat(pier.receivedEvents().last(CommandFailed.class).commandId()).isEqualTo(2);
    }

    @Test
    void shouldEchoBackTheStream()
    {
        ByteBuffer content = bufferWithMonotonicallyIncreasingLongs(10);
        Connected connected = connect();
        assertThat(connected).isNotNull();

        pier.handle(pier.command(connected, SendData.class).set(content.array(), 101));
        spin.spinUntil(() -> pier.receivedEvents().contains(DataReceived.class) && pier.receivedEvents().last(DataReceived.class).totalBytesReceived() == content.array().length);
        assertThat(extractedContent(pier.receivedEvents().all(DataReceived.class))).isEqualTo(content.array());
    }

    @AfterEach
    void tearDown()
    {
        drivingApplication.onStop();
        drivingApplication.work();
        transportApplicationUnderTest.onStop();
        transportApplicationUnderTest.work();
    }

    private Connected connect()
    {
        pier.handle(pier.command(Connect.class).set("localhost", port, (long)1, 50, RAW_STREAMING.type));
        pier.work();
        spin.spinUntil(() -> pier.receivedEvents().contains(Connected.class));
        return pier.lastResponse(Connected.class, 1);
    }

    private byte[] extractedContent(final List<DataReceived> receivedEvents)
    {
        int totalBytes = (int)receivedEvents.get(receivedEvents.size() - 1).totalBytesReceived();
        byte[] content = new byte[totalBytes];
        final MutableInteger bytesWrittenToContent = new MutableInteger(0);
        receivedEvents.forEach(
                dataReceived ->
                {
                    dataReceived.buffer().getBytes(dataReceived.offset(), content, bytesWrittenToContent.getAndAdd(dataReceived.length()), dataReceived.length());
                });

        return content;
    }
}