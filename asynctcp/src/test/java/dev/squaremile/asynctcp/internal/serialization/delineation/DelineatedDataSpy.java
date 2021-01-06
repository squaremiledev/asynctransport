package dev.squaremile.asynctcp.internal.serialization.delineation;

import java.util.ArrayList;
import java.util.List;

import org.agrona.DirectBuffer;

class DelineatedDataSpy implements DelineationHandler
{
    private final List<byte[]> received = new ArrayList<>();

    List<byte[]> received()
    {
        return received;
    }

    @Override
    public void onData(final DirectBuffer buffer, final int offset, final int length)
    {
        byte[] copy = new byte[length];
        buffer.getBytes(offset, copy);
        received.add(copy);
    }
}
