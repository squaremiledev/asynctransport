package dev.squaremile.transport.aeron;

import org.agrona.DirectBuffer;


import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;

class Fragment implements FragmentHandler
{
    DirectBuffer buffer;
    int offset;
    int length;

    @Override
    public void onFragment(final DirectBuffer buffer, final int offset, final int length, final Header header)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.length = length;
    }

    public FragmentHandler reset()
    {
        this.buffer = null;
        this.offset = 0;
        this.length = 0;
        return this;
    }

    public boolean hasData()
    {
        return buffer != null && length > 0;
    }
}
