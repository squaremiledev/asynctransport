package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.values.Delineation;
import dev.squaremile.asynctcp.transport.testfixtures.EventsSpy;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

class LongPingPongAppTest
{
    private static final int MESSAGES_CAP = 100;
    private final EventsSpy pingSpy = EventsSpy.spy();
    private final EventsSpy pongSpy = EventsSpy.spy();
    private final TransportApplicationFactory transportApplicationFactory = new AsyncTcp().transportAppFactory(NON_PROD_GRADE);
    private int port = freePort();
    private int numbersExchangedCount = 0;

    static Stream<Delineation> delineations()
    {
        return Stream.of(
                new Delineation(Delineation.Type.FIXED_LENGTH, 0, 8, ""),
                new Delineation(Delineation.Type.SHORT_BIG_ENDIAN_FIELD, 0, 0, ""),
                new Delineation(Delineation.Type.SHORT_BIG_ENDIAN_FIELD, 7, 0, ""),
                new Delineation(Delineation.Type.SHORT_BIG_ENDIAN_FIELD, 0, 5, ""),
                new Delineation(Delineation.Type.SHORT_LITTLE_ENDIAN_FIELD, 0, 0, ""),
                new Delineation(Delineation.Type.SHORT_LITTLE_ENDIAN_FIELD, 7, 0, ""),
                new Delineation(Delineation.Type.SHORT_LITTLE_ENDIAN_FIELD, 0, 5, ""),
                new Delineation(Delineation.Type.INT_BIG_ENDIAN_FIELD, 0, 0, ""),
                new Delineation(Delineation.Type.INT_BIG_ENDIAN_FIELD, 7, 0, ""),
                new Delineation(Delineation.Type.INT_BIG_ENDIAN_FIELD, 0, 5, ""),
                new Delineation(Delineation.Type.INT_LITTLE_ENDIAN_FIELD, 0, 0, ""),
                new Delineation(Delineation.Type.INT_LITTLE_ENDIAN_FIELD, 7, 0, ""),
                new Delineation(Delineation.Type.INT_LITTLE_ENDIAN_FIELD, 0, 5, "")
        );
    }

    @MethodSource("delineations")
    @ParameterizedTest
    void shouldExchangeLongs(final Delineation delineation)
    {
        ApplicationOnDuty pingApp = transportApplicationFactory.create(
                "ping",
                new LongPingPongAppFactory(
                        delineation,
                        MESSAGES_CAP,
                        port,
                        pingSpy,
                        number -> numbersExchangedCount++
                )
        );
        ApplicationOnDuty pongApp = transportApplicationFactory.create(
                "pong",
                new LongPongAppFactory(
                        delineation,
                        port,
                        pongSpy,
                        number -> numbersExchangedCount++
                )
        );
        Apps apps = new Apps(pingApp, pongApp);
        pingApp.onStart();
        apps.runUntil(() -> pingSpy.contains(StartedListening.class));
        pongApp.onStart();

        // When
        apps.runUntil(() -> pongSpy.contains(ConnectionClosed.class));

        // Then
        assertThat(numbersExchangedCount).isEqualTo(MESSAGES_CAP * 2);
    }
}
