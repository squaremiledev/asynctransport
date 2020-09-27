package dev.squaremile.asynctcp.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;


import dev.squaremile.asynctcp.serialization.api.SerializedCommandListener;
import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;

public class SerializedMessagesSpy implements SerializedEventListener, SerializedCommandListener
{
    private final List<WrittenEntries> entries = new ArrayList<>();
    private final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
    private int lastOffset = 0;

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        sourceBuffer.getBytes(sourceOffset, buffer, lastOffset, length);
        entries.add(new WrittenEntries(lastOffset, length));
        lastOffset += length;
    }

    public DirectBuffer buffer()
    {
        return buffer;
    }

    public int count()
    {
        return entries.size();
    }

    public WrittenEntries entry(final int index)
    {
        return entries.get(index);
    }

    public static class WrittenEntries
    {
        public final int offset;
        public final int length;

        WrittenEntries(final int offset, final int length)
        {
            this.offset = offset;
            this.length = length;
        }
    }
}
