package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;


import static dev.squaremile.asynctcp.serialization.internal.delineation.DataFixtures.assertEquals;

class LengthBasedDelineationTest
{
    private final DelineatedDataSpy delineatedDataSpy = new DelineatedDataSpy();
    private final Random random = new Random();

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, value = LengthEncoding.class, names = {"FIXED_LENGTH"})
    void shouldDelineateMessages(final LengthEncoding lengthEncoding)
    {
        final int padding = random.nextInt(20);
        int fixedMessageLength = random.nextInt(5);
        final byte[][] messages = StreamGenerator.messages(500, fixedMessageLength, (short)(fixedMessageLength + 30));

        deliverInChunks(
                new StreamGenerator(lengthEncoding, padding, fixedMessageLength, messages).generate(),
                new LengthBasedDelineation(lengthEncoding, padding, fixedMessageLength, delineatedDataSpy)
        );
        assertEquals(delineatedDataSpy.received(), messages);
    }


    private void deliverInChunks(final byte[] data, final LengthBasedDelineation delineation)
    {
        final Random random = new Random();
        final List<Chunk> chunksToDeliver = new ArrayList<>();
        int pos = 0;
        do
        {
            int startPos = pos;
            int currentChunkLength = Math.min(random.nextInt(9) + 1, data.length - startPos);
            ExpandableArrayBuffer dataBuffer = new ExpandableArrayBuffer();
            dataBuffer.putBytes(0, data, startPos, currentChunkLength);
            chunksToDeliver.add(new Chunk(dataBuffer, currentChunkLength));
            pos += currentChunkLength;
        }
        while (pos < data.length);

        for (int i = 0; i < chunksToDeliver.size(); i++)
        {
            Chunk chunk = chunksToDeliver.get(i);
            try
            {
                delineation.onData(chunk.directBuffer, 0, chunk.length);
            }
            catch (final Exception e)
            {
                throw e;
            }
        }
    }

    static class Chunk
    {
        DirectBuffer directBuffer;
        int length;

        public Chunk(final DirectBuffer directBuffer, final int length)
        {
            this.directBuffer = directBuffer;
            this.length = length;
        }
    }

}