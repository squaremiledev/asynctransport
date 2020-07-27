package com.michaelszymczak.sample.sockets.acceptancetests;

import com.michaelszymczak.sample.sockets.domain.api.commands.SendData;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.domain.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.support.TransportDriver;

import org.junit.jupiter.api.Test;


import static java.nio.charset.StandardCharsets.US_ASCII;


class ClientReceivesDataTest extends TransportTestBase
{


    @Test
    void shouldReceiveData()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);

        // Given
        final ConnectionAccepted conn = driver.listenAndConnect(clientTransport);

        // When
        serverTransport.handle(new SendData(conn.port(), conn.connectionId(), 3).set(bytes("foo"), 101));
        spinUntil(() -> !clientTransport.connectionEvents().all(DataReceived.class).isEmpty());

        // Then


    }


    private byte[] bytes(final String content)
    {
        return content.getBytes(US_ASCII);
    }

}
