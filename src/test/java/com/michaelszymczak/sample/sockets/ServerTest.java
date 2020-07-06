package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.net.ConnectException;

import com.michaelszymczak.sample.sockets.support.DelegatingServer;
import com.michaelszymczak.sample.sockets.support.FreePort;
import com.michaelszymczak.sample.sockets.support.ReadingClient;
import com.michaelszymczak.sample.sockets.support.ServerRun;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
