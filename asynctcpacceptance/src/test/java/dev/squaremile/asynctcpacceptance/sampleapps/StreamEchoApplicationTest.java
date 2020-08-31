package dev.squaremile.asynctcpacceptance.sampleapps;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.application.TransportAppLauncher;
import dev.squaremile.asynctcp.application.TransportApplication;
import dev.squaremile.asynctcp.domain.api.commands.Connect;
import dev.squaremile.asynctcp.domain.api.commands.SendData;
import dev.squaremile.asynctcp.domain.api.events.CommandFailed;
import dev.squaremile.asynctcp.domain.api.events.Connected;
import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.testfitures.app.WhiteboxApplication;

import static dev.squaremile.asynctcp.testfitures.FreePort.freePort;
import static dev.squaremile.asynctcp.testfitures.StringFixtures.byteArrayWith;
import static dev.squaremile.asynctcp.testfitures.StringFixtures.stringWith;
import static dev.squaremile.asynctcp.testfitures.Worker.runUntil;

class StreamEchoApplicationTest
{
    private final TransportApplication drivingApplication;
    private final TransportApplication transportApplication;
    private int port;
    private WhiteboxApplication whiteboxApplication;

    StreamEchoApplicationTest()
    {
        drivingApplication = new TransportAppLauncher().launch(
                transport ->
                {
                    whiteboxApplication = new WhiteboxApplication(transport);
                    return whiteboxApplication;
                });
        drivingApplication.onStart();
        port = freePort();
        transportApplication = new TransportAppLauncher().launch(transport -> new StreamEchoApplication(transport, port));
        transportApplication.onStart();
    }

    @Test
    void shouldListenUponStartAndStopListeningWhenStopped()
    {
        // When
        whiteboxApplication.underlyingtTansport().handle(whiteboxApplication.underlyingtTansport().command(Connect.class).set("localhost", port, 1, 50));
        spinUntil(() -> whiteboxApplication.events().contains(Connected.class));

        // Then
        assertThat(whiteboxApplication.events().all(Connected.class)).hasSize(1);

        // When
        transportApplication.onStop();
        whiteboxApplication.underlyingtTansport().handle(whiteboxApplication.underlyingtTansport().command(Connect.class).set("localhost", port, 2, 50));
        spinUntilAllowingFailures(() -> whiteboxApplication.events().contains(CommandFailed.class));

        // Then
        assertThat(whiteboxApplication.events().last(CommandFailed.class).commandId()).isEqualTo(2);
    }

    @Test
    void shouldEchoBackTheStream()
    {
        final byte[] _100_bytes = byteArrayWith(pos -> String.format("%9d%n", pos), 10);
        assertThat(_100_bytes).hasSize(100);
        Connected connected = connect();
        assertThat(connected).isNotNull();

        whiteboxApplication.underlyingtTansport().handle(whiteboxApplication.underlyingtTansport().command(connected, SendData.class).set(_100_bytes, 101));
        spinUntil(() -> whiteboxApplication.events().contains(DataReceived.class) && whiteboxApplication.events().last(DataReceived.class).totalBytesReceived() == 100);
        assertThat(stringWith(extractedContent(whiteboxApplication.events().all(DataReceived.class))))
                .isEqualTo(stringWith(_100_bytes));
    }


    @AfterEach
    void tearDown()
    {
        drivingApplication.onStop();
        transportApplication.onStop();
    }

    private Connected connect()
    {
        whiteboxApplication.underlyingtTansport().handle(whiteboxApplication.underlyingtTansport().command(Connect.class).set("localhost", port, 1, 50));
        spinUntil(() -> whiteboxApplication.events().contains(Connected.class));
        return whiteboxApplication.events().lastResponse(Connected.class, 1);
    }

    void spinUntil(final BooleanSupplier endCondition)
    {
        spinUntil(false, endCondition);
    }

    void spinUntilAllowingFailures(final BooleanSupplier endCondition)
    {
        spinUntil(true, endCondition);
    }

    void spinUntil(final boolean allowFailures, final BooleanSupplier endCondition)
    {
        runUntil(() ->
                 {
                     drivingApplication.work();
                     transportApplication.work();
                     if (!allowFailures)
                     {
                         throwOnFailures();
                     }
                     return endCondition.getAsBoolean();
                 });
    }

    private void throwOnFailures()
    {
        List<CommandFailed> failures = whiteboxApplication.events().all(CommandFailed.class);
        if (!failures.isEmpty())
        {
            throw new IllegalStateException("Failure occurred: " + failures);
        }
    }

    private byte[] extractedContent(final List<DataReceived> receivedEvents)
    {
        ByteBuffer actualContent = ByteBuffer.allocate((int)receivedEvents.get(receivedEvents.size() - 1).totalBytesReceived());
        receivedEvents.forEach(event -> event.copyDataTo(actualContent));
        return actualContent.array();
    }
}