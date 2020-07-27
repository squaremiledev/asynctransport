package com.michaelszymczak.sample.sockets.acceptancetests;

import com.michaelszymczak.sample.sockets.domain.api.commands.Connect;
import com.michaelszymczak.sample.sockets.domain.api.commands.SendData;
import com.michaelszymczak.sample.sockets.domain.api.events.CommandFailed;
import com.michaelszymczak.sample.sockets.domain.api.events.Connected;
import com.michaelszymczak.sample.sockets.domain.api.events.DataReceived;
import com.michaelszymczak.sample.sockets.domain.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.support.TransportDriver;
import com.michaelszymczak.sample.sockets.support.Worker;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static java.nio.charset.StandardCharsets.US_ASCII;

class ClientSendsDataTest extends TransportTestBase
{
    @Test
    void shouldSendData()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);
        StartedListening startedListening = driver.startListening();
        clientTransport.handle(new Connect().set(startedListening.port(), 100));
        Worker.runUntil(() ->
                        {
                            serverTransport.work();
                            clientTransport.work();
                            return !clientTransport.connectionEvents().all(Connected.class).isEmpty();
                        });
        Connected connected = clientTransport.connectionEvents().last(Connected.class);

        //When
        clientTransport.handle(clientTransport.command(connected, SendData.class).set(bytes("foo")));
        assertThat(clientTransport.events().all(CommandFailed.class)).isEmpty();
        assertThat(serverTransport.events().all(CommandFailed.class)).isEmpty();

        // Then
        Worker.runUntil(() ->
                        {
                            serverTransport.work();
                            clientTransport.work();
                            assertThat(clientTransport.events().all(CommandFailed.class)).isEmpty();
                            assertThat(serverTransport.events().all(CommandFailed.class)).isEmpty();
                            return !serverTransport.connectionEvents().all(DataReceived.class).isEmpty();
                        });
        assertThat(serverTransport.connectionEvents().all(DataReceived.class)).hasSize(1);
        DataReceived dataReceived = serverTransport.connectionEvents().last(DataReceived.class);
        assertThat(dataReceived.length()).isEqualTo(3);
        byte[] content = new byte[dataReceived.length()];
        dataReceived.copyDataTo(content);
        assertThat(content).isEqualTo(bytes("foo"));
    }

    private byte[] bytes(final String content)
    {
        return content.getBytes(US_ASCII);
    }
}
