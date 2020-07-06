package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.net.ConnectException;

import com.michaelszymczak.sample.sockets.support.DelegatingServer;
import com.michaelszymczak.sample.sockets.support.FreePort;
import com.michaelszymczak.sample.sockets.support.ReadingClient;
import com.michaelszymczak.sample.sockets.support.ServerRun;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static com.michaelszymczak.sample.sockets.support.ServerRun.startServer;

class ServerTest
{
    @Test
    void shouldAcceptConnections()
    {
        // Given
        runTest((reactiveSocket, client) ->
                {
                    // When
                    final int port = FreePort.freePort(0);
                    reactiveSocket.listen(port);

                    // Then
                    client.connectedTo(port);
                }
        );
    }

    @Test
    void shouldNotAcceptIfNotAsked()
    {
        runTest((reactiveSocket, client) -> assertThrows(ConnectException.class, () -> client.connectedTo(FreePort.freePort(0))));
    }

    // timeout required
    @Test
    void shouldNotAcceptIfListeningOnAnotherPort()
    {
        runTest((reactiveSocket, client) ->
                {
                    // When
                    final int port = FreePort.freePort(0);
                    reactiveSocket.listen(port);

                    // Then
                    assertThrows(ConnectException.class, () -> client.connectedTo(FreePort.freePortOtherThan(port)));
                }
        );
    }

    @Test
    void shouldStopListeningWhenAsked()
    {
        runTest((reactiveSocket, client) ->
                {
                    // Given
                    final int port = FreePort.freePort(0);
                    final long requestId = reactiveSocket.listen(port);

                    // When
                    reactiveSocket.stopListening(requestId);

                    // Then
                    assertThrows(ConnectException.class, () -> client.connectedTo(port));
                }
        );
    }

    @Test
    void shouldIgnoreStopListeningCommandForNonExistingRequest()
    {
        runTest((reactiveSocket, client) ->
                {
                    // Given
                    final int port = FreePort.freePort(0);
                    final long requestId = reactiveSocket.listen(port);

                    // When
                    final long result = reactiveSocket.stopListening(requestId + 1);
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
            final ReactiveSocket reactiveSocket = new ReactiveSocket();
            try (
                    ServerRun ignored = startServer(new DelegatingServer(reactiveSocket));
                    ReadingClient client1 = new ReadingClient();
                    ReadingClient client2 = new ReadingClient()
            )
            {
                // When
                final int port1 = FreePort.freePort(0);
                reactiveSocket.listen(port1);
                final int port2 = FreePort.freePortOtherThan(port1);
                reactiveSocket.listen(port2);

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
    @Disabled
    void shouldUseRequestIdToFindThePortItShouldStopListeningOn()
    {
        try
        {
            final ReactiveSocket reactiveSocket = new ReactiveSocket();
            try (
                    ServerRun ignored = startServer(new DelegatingServer(reactiveSocket));
                    ReadingClient client = new ReadingClient()
            )
            {
                // Given
                final int port1 = FreePort.freePort(0);
                reactiveSocket.listen(port1);
                final int port2 = FreePort.freePort(0);
                final long requestId = reactiveSocket.listen(port2);
                final int port3 = FreePort.freePort(0);
                reactiveSocket.listen(port3);

                // When
                final long result = reactiveSocket.stopListening(requestId);
                assertNotEquals(-1, result);

                // Then
                client.connectedTo(port1);
                assertThrows(ConnectException.class, () -> client.connectedTo(port2));
                client.connectedTo(port3);

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
            final ReactiveSocket reactiveSocket = new ReactiveSocket();
            try (
                    ServerRun ignored = startServer(new DelegatingServer(reactiveSocket));
                    ReadingClient client = new ReadingClient()
            )
            {
                testScenario.test(reactiveSocket, client);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    interface TestScenario
    {
        void test(ReactiveSocket reactiveSocket, ReadingClient client) throws IOException;
    }

}
