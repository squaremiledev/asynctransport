package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.michaelszymczak.sample.sockets.commands.Listen;
import com.michaelszymczak.sample.sockets.events.Event;
import com.michaelszymczak.sample.sockets.events.StartedListening;
import com.michaelszymczak.sample.sockets.support.DelegatingServer;
import com.michaelszymczak.sample.sockets.support.Events;
import com.michaelszymczak.sample.sockets.support.ReadingClient;
import com.michaelszymczak.sample.sockets.support.ServerRun;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePortOtherThan;
import static com.michaelszymczak.sample.sockets.support.ServerRun.startServer;

class ServerTest
{

    private Events events = new Events();

    @Test
    void shouldAcceptConnections()
    {
        // Given
        runTest((reactiveConnections, client) ->
                {
                    // When
                    final int port = freePort();
                    reactiveConnections.handle(new Listen(7, port));

                    // Then
                    final StartedListening event = events.last(StartedListening.class);
                    assertEquals(0, event.sessionId());
                    assertEquals(7, event.commandId());
                    assertEventsEquals(events.events(), new StartedListening(7, 0));
                    client.connectedTo(port);
                }
        );
    }

    @Test
    void shouldNotAcceptIfNotAsked()
    {
        runTest((reactiveConnections, client) -> assertThrows(ConnectException.class, () -> client.connectedTo(freePort())));
    }

    // timeout required
    @Test
    void shouldNotAcceptIfListeningOnAnotherPort()
    {
        runTest((reactiveConnections, client) ->
                {
                    // When
                    final int port = freePort();
                    reactiveConnections.handle(new Listen(0, port));

                    // Then
                    assertThrows(ConnectException.class, () -> client.connectedTo(freePortOtherThan(port)));
                }
        );
    }

    @Test
    void shouldStopListeningWhenAsked()
    {
        runTest((reactiveConnections, client) ->
                {
                    // Given
                    final int port = freePort();
                    reactiveConnections.handle(new Listen(0, port));

                    // When
                    reactiveConnections.stopListening(events.last(StartedListening.class).sessionId());

                    // Then
                    assertThrows(ConnectException.class, () -> client.connectedTo(port));
                }
        );
    }

    @Test
    void shouldIgnoreStopListeningCommandForNonExistingRequest()
    {
        runTest((reactiveConnections, client) ->
                {
                    // Given
                    final int port = freePort();
                    reactiveConnections.handle(new Listen(0, port));

                    // When
                    final long result = reactiveConnections.stopListening(events.last(StartedListening.class).sessionId() + 1);
                    assertEquals(-1, result);

                    // Then
                    client.connectedTo(port);
                }
        );
    }

    @Test
    void shouldBeAbleToListenOnMoreThanOnePort()
    {
        try
        {
            final ReactiveConnections reactiveConnections = new ReactiveConnections(events);
            try (
                    ServerRun ignored = startServer(new DelegatingServer(reactiveConnections));
                    ReadingClient client1 = new ReadingClient();
                    ReadingClient client2 = new ReadingClient()
            )
            {
                // When
                final int port1 = freePort();
                reactiveConnections.handle(new Listen(0, port1));
                final int port2 = freePortOtherThan(port1);
                reactiveConnections.handle(new Listen(1, port2));

                // Then
                client1.connectedTo(port1);
                client2.connectedTo(port2);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldUseRequestIdToFindThePortItShouldStopListeningOn()
    {
        try
        {
            final ReactiveConnections reactiveConnections = new ReactiveConnections(events);
            try (
                    ServerRun ignored = startServer(new DelegatingServer(reactiveConnections));
                    ReadingClient client1 = new ReadingClient();
                    ReadingClient client2 = new ReadingClient();
                    ReadingClient client3 = new ReadingClient()
            )
            {
                // Given
                final int port1 = freePort();
                reactiveConnections.handle(new Listen(5, port1));
                final int port2 = freePortOtherThan(port1);
                reactiveConnections.handle(new Listen(6, port2));
                final int port3 = freePortOtherThan(port1, port2);
                reactiveConnections.handle(new Listen(7, port3));
                assertEventsEquals(
                        events.events(),
                        new StartedListening(5, 0),
                        new StartedListening(6, 1),
                        new StartedListening(7, 2)
                );

                // When
                final long result = reactiveConnections.stopListening(1);
                assertNotEquals(-1, result);

                // Then
                client1.connectedTo(port1);
                assertThrows(ConnectException.class, () -> client2.connectedTo(port2));
                client3.connectedTo(port3);

            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void runTest(final TestScenario testScenario)
    {
        try
        {
            final ReactiveConnections reactiveConnections = new ReactiveConnections(events);
            try (
                    ServerRun ignored = startServer(new DelegatingServer(reactiveConnections));
                    ReadingClient client = new ReadingClient()
            )
            {
                testScenario.test(reactiveConnections, client);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    private void assertEventsEquals(final List<Event> actualEvents, final Event... expectedEvents)
    {
        final RecursiveComparisonConfiguration recursiveComparisonConfiguration = new RecursiveComparisonConfiguration();
        recursiveComparisonConfiguration.strictTypeChecking(true);
        assertThat(new ArrayList<>(actualEvents))
                .usingRecursiveComparison(recursiveComparisonConfiguration)
                .isEqualTo(new ArrayList<>(Arrays.asList(expectedEvents)));
    }


    interface TestScenario
    {
        void test(ReactiveConnections reactiveConnections, ReadingClient client) throws IOException;
    }

}
