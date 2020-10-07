package dev.squaremile.asynctcpacceptance.sampleapps;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.internal.TransportApplication;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

class SmallMessagesTwoWayThroughputTest
{
    private static final int WARM_UP = 100_000;
    private static final int MESSAGES_EXCHANGED_WHEN_MEASURED = 500_000;
    private static final int MESSAGES_CAP = MESSAGES_EXCHANGED_WHEN_MEASURED + WARM_UP;
    private final TransportApplicationFactory transportApplicationFactory = new AsyncTcp().transportAppFactory(NON_PROD_GRADE);
    private final MyEventListener stateListener = new MyEventListener();
    private int port = freePort();
    private int numbersExchangedCount = 0;

    @Test
    void shouldExchangeLongs()
    {
        TransportApplication pingApp = transportApplicationFactory.create("ping", new LongPingPongAppFactory(MESSAGES_CAP, port, stateListener, number -> numbersExchangedCount++));
        TransportApplication pongApp = transportApplicationFactory.create("pong", new LongPongAppFactory(port, stateListener, number -> numbersExchangedCount++));
        Apps apps = new Apps(pingApp, pongApp);
        pingApp.onStart();
        apps.runUntil(stateListener::hasStartedListening);
        pongApp.onStart();

        // When
        while (numbersExchangedCount < WARM_UP)
        {
            pingApp.work();
            pongApp.work();
        }
        long timeBeforeMs = System.currentTimeMillis();
        while (!stateListener.hasClosedConnection())
        {
            pingApp.work();
            pongApp.work();
        }
        long timeAfterMs = System.currentTimeMillis();
        long timeElapsedMs = timeAfterMs - timeBeforeMs;
        long msgsPerSecond = (MESSAGES_EXCHANGED_WHEN_MEASURED * 1000) / timeElapsedMs;


        // Then
        assertThat(numbersExchangedCount).isEqualTo(MESSAGES_CAP * 2);
        assertThat(timeElapsedMs).isLessThan(30_000);
        assertThat(msgsPerSecond).isGreaterThan(10_000);
        System.out.println("Time elapsed: " + timeElapsedMs);
        System.out.println("msg/s: " + msgsPerSecond);
    }

    private static class MyEventListener implements EventListener
    {

        private boolean hasStartedListening;
        private boolean hasClosedConnection;

        @Override
        public void onEvent(final Event event)
        {
            if (event instanceof StartedListening)
            {
                hasStartedListening = true;
            }
            if (event instanceof ConnectionClosed)
            {
                hasClosedConnection = true;
            }
        }

        public boolean hasStartedListening()
        {
            return hasStartedListening;
        }

        public boolean hasClosedConnection()
        {
            return hasClosedConnection;
        }
    }
}
