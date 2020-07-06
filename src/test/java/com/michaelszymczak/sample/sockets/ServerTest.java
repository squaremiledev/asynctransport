package com.michaelszymczak.sample.sockets;

import java.net.ConnectException;

import com.michaelszymczak.sample.sockets.support.DelegatingServer;
import com.michaelszymczak.sample.sockets.support.FreePort;
import com.michaelszymczak.sample.sockets.support.ReadingClient;
import com.michaelszymczak.sample.sockets.support.ServerRun;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;


import static com.michaelszymczak.sample.sockets.support.ServerRun.startServer;

class ServerTest
{
    @Test
    void shouldAcceptConnections() throws Exception
    {
        // Given
        final ReactiveSocket reactiveSocket = new ReactiveSocket();
        try (
                ServerRun serverRun = startServer(new DelegatingServer(reactiveSocket));
                ReadingClient client = new ReadingClient()
        )
        {
            // When
            final int port = FreePort.freePort(0);
            reactiveSocket.accept(port);

            // Then
            client.connectedTo(port);
        }
    }

    @Test
    void shouldNotAcceptIfNotAsked() throws Exception
    {
        // Given
        final ReactiveSocket reactiveSocket = new ReactiveSocket();
        try (
                ServerRun serverRun = startServer(new DelegatingServer(reactiveSocket));
                ReadingClient client = new ReadingClient()
        )
        {
            // Then
            assertThrows(ConnectException.class, () -> client.connectedTo(FreePort.freePort(0)));
        }
    }

    // timeout required
    @Test
    void shouldNotAcceptIfListeningOnAnotherPort() throws Exception
    {
        // Given
        final ReactiveSocket reactiveSocket = new ReactiveSocket();
        try (
                ServerRun serverRun = startServer(new DelegatingServer(reactiveSocket));
                ReadingClient client = new ReadingClient()
        )
        {
            // When
            final int port = FreePort.freePort(0);
            reactiveSocket.accept(port);

            // Then
            assertThrows(ConnectException.class, () -> client.connectedTo(FreePort.freePortOtherThan(port)));
        }
    }
}
