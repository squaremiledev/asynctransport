package dev.squaremile.asynctcpacceptance.sampleapps;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.setup.TransportApplication;
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

    @Test
    void shouldExchangeLongs()
    {
        TransportApplication pingApp = transportApplicationFactory.create("ping", new LongPingAppFactory(MESSAGES_CAP, port, pingSpy, number -> numbersExchangedCount++));
        TransportApplication pongApp = transportApplicationFactory.create("pong", new LongPongAppFactory(port, pongSpy, number -> numbersExchangedCount++));
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
