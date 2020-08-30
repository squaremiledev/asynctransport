package dev.squaremile.asynctcpacceptance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.domain.api.commands.Connect;
import dev.squaremile.asynctcp.domain.api.commands.SendData;
import dev.squaremile.asynctcp.domain.api.events.CommandFailed;
import dev.squaremile.asynctcp.domain.api.events.Connected;
import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.StartedListening;

import static java.nio.charset.StandardCharsets.US_ASCII;

class ClientSendsDataTest extends TransportTestBase
{
    @Test
    void shouldSendData()
    {
        final TransportDriver driver = new TransportDriver(serverTransport);
        StartedListening startedListening = driver.startListening();
        clientTransport.handle(clientTransport.command(Connect.class).set(startedListening.port(), 100));
        spinUntil(() -> !clientTransport.connectionEvents().all(Connected.class).isEmpty());
        Connected connected = clientTransport.connectionEvents().last(Connected.class);

        //When
        clientTransport.handle(clientTransport.command(connected, SendData.class).set(bytes("foo")));
        assertThat(clientTransport.events().all(CommandFailed.class)).isEmpty();
        assertThat(serverTransport.events().all(CommandFailed.class)).isEmpty();

        // Then
        spinUntil(() -> !serverTransport.connectionEvents().all(DataReceived.class).isEmpty());
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
