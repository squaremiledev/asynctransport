package dev.squaremile.asynctcp.api.transport.events;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.transport.values.ConnectionIdValue;

class MessageReceivedTest
{
    @Test
    void shouldCreateCopy()
    {
        // Given
        MessageReceived messageReceived = new MessageReceived(new ConnectionIdValue(8899, 4)).set(new UnsafeBuffer(new byte[]{1, 2, 3, 4, 5, 6, 7}), 2, 3);

        // When
        MessageReceived copy = messageReceived.copy();

        // Then
        assertThat(copy.port()).isEqualTo(messageReceived.port());
        assertThat(copy.connectionId()).isEqualTo(messageReceived.connectionId());
        assertThat(dataIn(copy)).isEqualTo(dataIn(messageReceived));
    }

    private byte[] dataIn(final MessageReceived message)
    {
        byte[] content = new byte[message.length()];
        message.buffer().getBytes(message.offset(), content);
        return content;
    }
}