package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.net.ConnectException;

import com.michaelszymczak.sample.sockets.api.Transport;
import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.commands.StopListening;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.api.events.StoppedListening;
import com.michaelszymczak.sample.sockets.api.events.TransportCommandFailed;
import com.michaelszymczak.sample.sockets.nio.NIOBackedTransport;
import com.michaelszymczak.sample.sockets.support.DelegatingServer;
import com.michaelszymczak.sample.sockets.support.SampleClient;
import com.michaelszymczak.sample.sockets.support.ServerRun;
import com.michaelszymczak.sample.sockets.support.StatusEventsSpy;
import com.michaelszymczak.sample.sockets.support.SynchronizedTransportEvents;
import com.michaelszymczak.sample.sockets.support.TestableTransport;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static com.michaelszymczak.sample.sockets.support.Assertions.assertEqual;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePortOtherThan;
import static com.michaelszymczak.sample.sockets.support.ServerRun.startServer;

class ListeningTransportTest
{

    private SynchronizedTransportEvents events = new SynchronizedTransportEvents();

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
                    assertEqual(events.events(), new StartedListening(port, 7));
                    client.connectedTo(port);
                }
        );
    }

    @Test
    void shouldNotAcceptIfNotAsked()
    {
        runTest((transport, client) -> assertThrows(ConnectException.class, () -> client.connectedTo(freePort())));
    }

    @Test
    void shouldStopListeningWhenAsked()
    {
        runTest((transport, client) ->
                {
                    // Given
                    transport.handle(new Listen(0, freePort()));
                    final int port = events.last(StartedListening.class).port();

                    // When
                    transport.handle(new StopListening(9, port));

                    // Then
                    assertEquals(port, events.last(StoppedListening.class).port());
                    assertEquals(9, events.last(StoppedListening.class).commandId());
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
                    assertThat(events.last(TransportCommandFailed.class).commandId()).isEqualTo(4);
                    assertThat(events.last(TransportCommandFailed.class).port()).isEqualTo(anotherPort);
                    client.connectedTo(port);
                }
        );
    }

    @Test
    void shouldBeAbleToListenOnMoreThanOnePort()
    {
        try
        {
            final NIOBackedTransport transport = new NIOBackedTransport(events, new StatusEventsSpy());
            try (
                    ServerRun ignored = startServer(new DelegatingServer(transport));
                    SampleClient client1 = new SampleClient();
                    SampleClient client2 = new SampleClient()
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
            final TestableTransport<SynchronizedTransportEvents> transport = new TestableTransport<>(events, new StatusEventsSpy());
            try (
                    ServerRun ignored = startServer(new DelegatingServer(transport));
                    SampleClient client1 = new SampleClient();
                    SampleClient client2 = new SampleClient();
                    SampleClient client3 = new SampleClient()
            )
            {
                // Given
                final int port1 = freePort();
                final int port2 = freePortOtherThan(port1);
                final int port3 = freePortOtherThan(port1, port2);
                // sometimes one of the following assertions fail
                // is it because there is still an open port from the previous test?
                assertThrows(ConnectException.class, () -> new SampleClient().connectedTo(port1));
                assertThrows(ConnectException.class, () -> new SampleClient().connectedTo(port2));
                assertThrows(ConnectException.class, () -> new SampleClient().connectedTo(port3));

                transport.handle(new Listen(5, port1));
                transport.handle(new Listen(6, port2));
                transport.handle(new Listen(7, port3));
                assertEqual(events.events(), new StartedListening(port1, 5), new StartedListening(port2, 6), new StartedListening(port3, 7));

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
            final TestableTransport<SynchronizedTransportEvents> transport = new TestableTransport<>(events, new StatusEventsSpy());
            try (
                    ServerRun ignored = startServer(new DelegatingServer(transport));
                    SampleClient client = new SampleClient()
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


    interface TestScenario
    {
        void test(Transport transport, SampleClient client) throws IOException;
    }

}
