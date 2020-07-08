package com.michaelszymczak.sample.sockets.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SampleClient implements AutoCloseable
{

    private final Socket socket;
    private final int timeoutMs;

    public SampleClient()
    {
        this.timeoutMs = 100;
        this.socket = new Socket();
    }

    public SampleClient connectedTo(final int port) throws IOException
    {
        socket.connect(new InetSocketAddress("127.0.0.1", port), timeoutMs);
        socket.setSoTimeout(timeoutMs);
        return this;
    }

    public byte[] read(final int contentSize, final int allocatedSize) throws IOException
    {
        int bytesRead = 0;
        final byte[] receivedContent = new byte[allocatedSize];
        do
        {
            bytesRead += socket.getInputStream().read(receivedContent);
        }
        while (bytesRead < contentSize);

        return receivedContent;
    }

    @Override
    public void close() throws IOException
    {
        socket.close();
    }
}
