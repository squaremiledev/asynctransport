package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.michaelszymczak.sample.sockets.connection.Channel;

public class SocketBackedChannel implements Channel
{
    private final SocketChannel socketChannel;

    public SocketBackedChannel(final SocketChannel socketChannel)
    {
        this.socketChannel = socketChannel;
    }

    @Override
    public int write(final ByteBuffer src) throws IOException
    {
        return socketChannel.write(src);
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException
    {
        return socketChannel.read(dst);
    }

    @Override
    public void close() throws Exception
    {
        socketChannel.close();
    }
}
