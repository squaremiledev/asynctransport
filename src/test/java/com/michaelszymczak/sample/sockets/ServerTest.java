package com.michaelszymczak.sample.sockets;

import com.michaelszymczak.sample.sockets.support.DelegatingServer;
import com.michaelszymczak.sample.sockets.support.ReadingClient;
import com.michaelszymczak.sample.sockets.support.ServerRun;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


import static com.michaelszymczak.sample.sockets.support.ServerRun.startServer;
import static com.michaelszymczak.sample.sockets.support.StringFixtures.byteArrayWith;
import static com.michaelszymczak.sample.sockets.support.StringFixtures.stringWith;

class ServerTest
{
    @Test
    void shouldAcceptConnections() throws Exception
    {
        // Given
        try (
                ServerRun serverRun = startServer(DelegatingServer.returningUponConnection(0, byteArrayWith("hello!\n")));
                ReadingClient client = new ReadingClient()
        )
        {
            // When
            final byte[] actualReadContent = client.connectedTo(serverRun.serverPort())
                    .read(byteArrayWith("hello!\n").length, 10);

            // Then
            assertEquals(stringWith(byteArrayWith("hello!\n"), 10), stringWith(actualReadContent));
        }
    }
}
