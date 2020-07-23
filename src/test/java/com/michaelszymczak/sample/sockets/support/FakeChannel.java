package com.michaelszymczak.sample.sockets.support;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.michaelszymczak.sample.sockets.domain.connection.Channel;

public class FakeChannel implements Channel
{
    private boolean isOpen = true;
    private int maxBytesReadInOneGo = 0;
    private int maxBytesWrittenInOneGo = 0;
    private List<String> writeAttempts = new ArrayList<>();

    public FakeChannel maxBytesReadInOneGo(int value)
    {
        this.maxBytesWrittenInOneGo = value;
        return this;
    }

    public FakeChannel maxBytesWrittenInOneGo(int value)
    {
        this.maxBytesReadInOneGo = value;
        return this;
    }

    public FakeChannel allBytesWrittenInOneGo()
    {
        maxBytesWrittenInOneGo(Integer.MAX_VALUE);
        return this;
    }

    public FakeChannel isOpen(boolean value)
    {
        isOpen = value;
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
        return maxBytesWrittenInOneGo;
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

    public List<String> attemptedToWrite()
    {
        return writeAttempts;
    }
}
