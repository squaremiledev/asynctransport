package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.List;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.application.TransportAppLauncher;
import dev.squaremile.asynctcp.application.TransportApplication;
import dev.squaremile.asynctcp.domain.api.commands.Connect;
import dev.squaremile.asynctcp.domain.api.events.CommandFailed;
import dev.squaremile.asynctcp.domain.api.events.Connected;
import dev.squaremile.asynctcp.testfitures.app.WhiteboxApplication;

import static dev.squaremile.asynctcp.testfitures.FreePort.freePort;
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
    @Disabled
    void shouldEchoBackTheStream()
    {
        Connected connected = connect();
        assertThat(connected).isNotNull();
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
}