package com.michaelszymczak.sample.sockets;

import com.michaelszymczak.sample.sockets.support.DelegatingServer;
import com.michaelszymczak.sample.sockets.support.ReadingClient;
import com.michaelszymczak.sample.sockets.support.ServerRun;

import org.junit.jupiter.api.Test;


import static com.michaelszymczak.sample.sockets.support.ServerRun.startServer;

class ServerTest
{
    @Test
    void shouldAcceptConnections() throws Exception
    {
        // Given
        try (
                ServerRun serverRun = startServer(new DelegatingServer(0));
                ReadingClient client = new ReadingClient()
        )
        {
            // Then
            client.connectedTo(serverRun.serverPort());
        }
    }
}
