package com.michaelszymczak.sample.sockets.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ConnectedSocket implements AutoCloseable
{
    public final SocketChannel channel;
    private final int port;
    private final long connectionId;
    private final int remotePort;
    private final ByteBuffer sendBuffer;

    public ConnectedSocket(final int port, final long connectionId, final int remotePort, final SocketChannel channel)
    {
        this.port = port;
        this.connectionId = connectionId;
        this.remotePort = remotePort;
        this.channel = channel;
        sendBuffer = ByteBuffer.allocate(1);
    }

    public int port()
    {
        return port;
    }

    public long connectionId()
    {
        return connectionId;
    }

    public int remotePort()
    {
        return remotePort;
    }

    public SocketChannel channel()
    {
        return channel;
    }

    public void write() throws IOException
    {
        channel.write(sendBuffer);
    }

    @Override
    public void close()
    {
        Resources.close(channel);
    }
}
