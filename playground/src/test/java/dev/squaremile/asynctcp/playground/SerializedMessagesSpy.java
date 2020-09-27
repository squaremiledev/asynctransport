package dev.squaremile.asynctcp.playground;

import java.util.ArrayList;
import java.util.List;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;


import dev.squaremile.asynctcp.serialization.SerializedCommandListener;
import dev.squaremile.asynctcp.serialization.SerializedEventListener;

class SerializedMessagesSpy implements SerializedEventListener, SerializedCommandListener
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

    DirectBuffer buffer()
    {
        return buffer;
    }

    int count()
    {
        return entries.size();
    }

    WrittenEntries entry(final int index)
    {
        return entries.get(index);
    }

    static class WrittenEntries
    {
        final int offset;
        final int length;

        WrittenEntries(final int offset, final int length)
        {
            this.offset = offset;
            this.length = length;
        }
    }
}
