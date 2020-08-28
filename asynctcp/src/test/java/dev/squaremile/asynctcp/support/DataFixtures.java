package dev.squaremile.asynctcp.support;

import java.nio.ByteBuffer;
import java.util.List;

public class DataFixtures
{

    public static byte[] concatenatedData(final List<byte[]> allChunks)
    {
        int totalSize = allChunks.stream().mapToInt(chunk -> chunk.length).sum();
        byte[] content = new byte[totalSize];
        ByteBuffer received = ByteBuffer.wrap(content);
        for (final byte[] chunk : allChunks)
        {
            received.put(chunk);
        }
        return content;
    }
}
