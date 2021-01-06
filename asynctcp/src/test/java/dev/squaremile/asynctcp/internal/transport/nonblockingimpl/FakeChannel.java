package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


import dev.squaremile.asynctcp.internal.transport.domain.connection.Channel;

class FakeChannel implements Channel
{
    private boolean isOpen = true;
    private int maxBytesReadInOneGo = 0;
    private List<String> writeAttempts = new ArrayList<>();

    FakeChannel maxBytesWrittenInOneGo(int value)
    {
        this.maxBytesReadInOneGo = value;
        return this;
    }

    FakeChannel allBytesWrittenInOneGo()
    {
        maxBytesWrittenInOneGo(Integer.MAX_VALUE);
        return this;
    }

    @Override
    public int write(final ByteBuffer src)
    {
        final int bytesWritten = Math.min(maxBytesReadInOneGo, src.remaining());
        final byte[] target = new byte[bytesWritten];
        src.get(target);
        writeAttempts.add(new String(target, StandardCharsets.US_ASCII));
        return bytesWritten;
    }

    @Override
    public int read(final ByteBuffer dst)
    {
        return 0;
    }

    @Override
    public boolean isOpen()
    {
        return isOpen;
    }

    @Override
    public void close()
    {
        isOpen = false;
    }

    List<String> attemptedToWrite()
    {
        return writeAttempts;
    }
}
