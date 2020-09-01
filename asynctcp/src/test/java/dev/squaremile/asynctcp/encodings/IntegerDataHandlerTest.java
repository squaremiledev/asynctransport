package dev.squaremile.asynctcp.encodings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.domain.api.events.DataReceived;

import static java.nio.ByteBuffer.wrap;

class IntegerDataHandlerTest
{
    private MessageReceivedSpy messageReceivedSpy = new MessageReceivedSpy();
    private DataReceived dataReceived = new DataReceived(8888, 1, 0, 0, 100, wrap(new byte[100]));
    private IntegerDataHandler handler = new IntegerDataHandler(1, messageReceivedSpy);

    @Test
    void shouldNotNotifyOnNoData()
    {
        dataReceived.prepare();
        dataReceived.commit(0, 0);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).isEmpty();
    }

    @Test
    void shouldNotifyAboutReceivedInteger()
    {
        dataReceived.prepare().putInt(1295619689);
        dataReceived.commit(4, 4);

        // When
        handler.onDataReceived(dataReceived);

        // Then
        assertThat(messageReceivedSpy.all()).hasSize(1);
        assertThat(messageReceivedSpy.asPdus().get(0)).hasSize(4);
        assertThat(wrap(messageReceivedSpy.asPdus().get(0)).getInt()).isEqualTo(1295619689);
    }
}