package com.michaelszymczak.sample.sockets.support;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.michaelszymczak.sample.sockets.connection.Channel;

public class FakeChannel implements Channel
{

    @Override
    public void close() throws Exception
    {

    }

    @Override
    public int write(final ByteBuffer src) throws IOException
    {
        return 0;
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException
    {
        return 0;
    }
}
