package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;


import dev.squaremile.asynctcp.transport.api.values.LengthEncoding;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class StreamGenerator
{
    private final LengthEncoding lengthEncoding;
    private final byte[] padding;
    private final byte[][] messages;
    private final int additionalMessageLength;

    public StreamGenerator(final LengthEncoding lengthEncoding, final int padding, final int additionalMessageLength, final byte[][] messages)
    {
        this.lengthEncoding = lengthEncoding;
        this.messages = messages;
        this.padding = new byte[padding];
        this.additionalMessageLength = additionalMessageLength;
    }

    static byte[][] messages(final int maxNumberOfMessages, final int minLengthOfTheMessage, final short maxLengthOfTheMessage)
    {
        final Random random = new Random();
        final int messagesCount = random.nextInt(maxNumberOfMessages);
        final byte[][] messages = new byte[messagesCount][];

        for (int i = 0; i < messagesCount; i++)
        {
            messages[i] = message(random, minLengthOfTheMessage, maxLengthOfTheMessage);
        }
        return messages;
    }

    private static byte[] message(final Random random, final int minLengthOfTheMessage, final short maxLengthOfTheMessage)
    {
        short length = (short)(minLengthOfTheMessage + random.nextInt(maxLengthOfTheMessage - minLengthOfTheMessage) + 1);
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
                rawDataLength
                ];
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
        switch (lengthEncoding)
        {
            case SHORT_BIG_ENDIAN_FIELD:
                buffer.order(BIG_ENDIAN);
                buffer.putShort((short)(message.length - additionalMessageLength));
                break;
            case SHORT_LITTLE_ENDIAN_FIELD:
                buffer.order(LITTLE_ENDIAN);
                buffer.putShort((short)(message.length - additionalMessageLength));
                break;
            case INT_BIG_ENDIAN_FIELD:
                buffer.order(BIG_ENDIAN);
                buffer.putInt(message.length - additionalMessageLength);
                break;
            case INT_LITTLE_ENDIAN_FIELD:
                buffer.order(LITTLE_ENDIAN);
                buffer.putInt(message.length - additionalMessageLength);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
