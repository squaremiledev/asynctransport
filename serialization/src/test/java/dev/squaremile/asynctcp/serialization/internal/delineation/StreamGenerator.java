package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;


import static dev.squaremile.asynctcp.serialization.internal.delineation.LengthEncoding.INT_BIG_ENDIAN_FIELD;
import static java.nio.ByteOrder.BIG_ENDIAN;

public class StreamGenerator
{
    private final LengthEncoding lengthEncoding;
    private final byte[] padding;
    private final byte[][] messages;

    public StreamGenerator(final LengthEncoding lengthEncoding, final int padding, final byte[][] messages)
    {
        this.lengthEncoding = lengthEncoding;
        this.messages = messages;
        this.padding = new byte[padding];
    }

    static byte[][] messages(final int maxNumberOfMessages, final int maxLengthOfTheMessage)
    {
        final Random random = new Random();
        final int messagesCount = random.nextInt(maxNumberOfMessages);
        final byte[][] messages = new byte[messagesCount][];

        for (int i = 0; i < messagesCount; i++)
        {
            messages[i] = message(random, maxLengthOfTheMessage);
        }
        return messages;
    }

    private static byte[] message(final Random random, final int maxLengthOfTheMessage)
    {
        int length = random.nextInt(maxLengthOfTheMessage) + 1;
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    byte[] generate()
    {
        final int rawDataLength = Arrays.stream(messages).mapToInt(message -> message.length).sum();
        final byte[] stream = new byte[
                messages.length * padding.length +
                messages.length * lengthEncoding.lengthFieldLength +
                rawDataLength];
        final ByteBuffer buffer = ByteBuffer.wrap(stream);
        for (byte[] message : messages)
        {
            buffer.put(padding);
            encodeLength(buffer, message);
            buffer.put(message);
        }
        return stream;
    }

    private void encodeLength(final ByteBuffer buffer, final byte[] message)
    {
        if (lengthEncoding == INT_BIG_ENDIAN_FIELD)
        {
            buffer.order(BIG_ENDIAN);
            buffer.putInt(message.length);
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }
}
