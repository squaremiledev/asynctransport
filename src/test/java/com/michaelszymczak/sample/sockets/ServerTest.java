package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.michaelszymczak.sample.sockets.commands.Listen;
import com.michaelszymczak.sample.sockets.commands.StopListening;
import com.michaelszymczak.sample.sockets.events.CommandFailed;
import com.michaelszymczak.sample.sockets.events.StartedListening;
import com.michaelszymczak.sample.sockets.events.StoppedListening;
import com.michaelszymczak.sample.sockets.events.TransportEvent;
import com.michaelszymczak.sample.sockets.support.DelegatingServer;
import com.michaelszymczak.sample.sockets.support.ReadingClient;
import com.michaelszymczak.sample.sockets.support.ServerRun;
import com.michaelszymczak.sample.sockets.support.TransportEvents;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePortOtherThan;
import static com.michaelszymczak.sample.sockets.support.ServerRun.startServer;

class ServerTest
{

    private TransportEvents events = new TransportEvents();

    @Test
    void shouldAcceptConnections()
    {
        // Given
        runTest((transport, client) ->
                {
                    // When
                    final int port = freePort();
                    transport.handle(new Listen(7, port));

                    // Then
                    final StartedListening event = events.last(StartedListening.class);
                    assertEquals(port, event.port());
                    assertEquals(7, event.commandId());
                    assertEventsEquals(events.events(), new StartedListening(port, 7));
                    client.connectedTo(port);
                }
        );
    }

    @Test
    void shouldNotAcceptIfNotAsked()
    {
        runTest((transport, client) -> assertThrows(ConnectException.class, () -> client.connectedTo(freePort())));
    }

    // timeout required
    @Test
    void shouldNotAcceptIfListeningOnAnotherPort()
    {
        runTest((transport, client) ->
                {
                    // When
                    final int port = freePort();
                    transport.handle(new Listen(0, port));

                    // Then
                    assertThrows(ConnectException.class, () -> client.connectedTo(freePortOtherThan(port)));
                }
        );
    }

    @Test
    void shouldStopListeningWhenAsked()
    {
        runTest((transport, client) ->
                {
                    // Given
                    final int port = freePort();
                    transport.handle(new Listen(0, port));

                    // When
                    transport.handle(new StopListening(9, port));

                    // Then
                    assertThrows(ConnectException.class, () -> client.connectedTo(port));
                }
        );
    }

    @Test
    void shouldIgnoreStopListeningCommandForNonExistingRequest()
    {
        runTest((transport, client) ->
                {
                    // Given
                    final int port = freePort();
                    final int anotherPort = freePortOtherThan(port);
                    transport.handle(new Listen(2, port));

                    // When
                    transport.handle(new StopListening(4, anotherPort));

                    // Then
                    assertThat(events.last(CommandFailed.class).commandId()).isEqualTo(4);
                    assertThat(events.last(CommandFailed.class).port()).isEqualTo(anotherPort);
                    client.connectedTo(port);
                }
        );
    }

    @Test
    void shouldBeAbleToListenOnMoreThanOnePort()
    {
        try
        {
            final Transport transport = new Transport(events);
            try (
                    ServerRun ignored = startServer(new DelegatingServer(transport));
                    ReadingClient client1 = new ReadingClient();
                    ReadingClient client2 = new ReadingClient()
            )
            {
                // When
                final int port1 = freePort();
                transport.handle(new Listen(0, port1));
                final int port2 = freePortOtherThan(port1);
                transport.handle(new Listen(1, port2));

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
            final Transport transport = new Transport(events);
            try (
                    ServerRun ignored = startServer(new DelegatingServer(transport));
                    ReadingClient client1 = new ReadingClient();
                    ReadingClient client2 = new ReadingClient();
                    ReadingClient client3 = new ReadingClient()
            )
            {
                // Given
                final int port1 = freePort();
                transport.handle(new Listen(5, port1));
                final int port2 = freePortOtherThan(port1);
                transport.handle(new Listen(6, port2));
                final int port3 = freePortOtherThan(port1, port2);
                transport.handle(new Listen(7, port3));
                assertEventsEquals(
                        events.events(),
                        new StartedListening(port1, 5),
                        new StartedListening(port2, 6),
                        new StartedListening(port3, 7)
                );

                // When
                transport.handle(new StopListening(9, port2));

                // Then
                assertThat(events.last(StoppedListening.class).commandId()).isEqualTo(9);
                assertThat(events.last(StoppedListening.class).port()).isEqualTo(port2);
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
            final Transport transport = new Transport(events);
            try (
                    ServerRun ignored = startServer(new DelegatingServer(transport));
                    ReadingClient client = new ReadingClient()
            )
            {
                testScenario.test(transport, client);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    private void assertEventsEquals(final List<TransportEvent> actualEvents, final TransportEvent... expectedEvents)
    {
        final RecursiveComparisonConfiguration recursiveComparisonConfiguration = new RecursiveComparisonConfiguration();
        recursiveComparisonConfiguration.strictTypeChecking(true);
        assertThat(new ArrayList<>(actualEvents))
                .usingRecursiveComparison(recursiveComparisonConfiguration)
                .isEqualTo(new ArrayList<>(Arrays.asList(expectedEvents)));
    }


    interface TestScenario
    {
        void test(Transport transport, ReadingClient client) throws IOException;
    }

}
