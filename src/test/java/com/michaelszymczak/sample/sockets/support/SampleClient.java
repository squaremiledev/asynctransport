package com.michaelszymczak.sample.sockets.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import com.michaelszymczak.sample.sockets.nio.Resources;

public class SampleClient implements AutoCloseable
{

    private final Socket socket;
    private final int timeoutMs;

    public SampleClient() throws SocketException
    {
        this.timeoutMs = 100;
        this.socket = new Socket();
        this.socket.setSoTimeout(timeoutMs);
    }

    public SampleClient connectedTo(final int port) throws IOException
    {
        return connectedTo(port, -1);
    }

    public SampleClient connectedTo(final int port, final int localPort) throws IOException
    {
        if (localPort != -1)
        {
            socket.bind(new InetSocketAddress("127.0.0.1", localPort));
        }
        socket.connect(new InetSocketAddress("127.0.0.1", port), timeoutMs);
        return this;
    }

    public byte[] read(final int contentSize, final int allocatedSize) throws IOException
    {
        if (!socket.isConnected())
        {
            throw new IllegalStateException("Client socket not connected");
        }
        int bytesRead = 0;
        final byte[] receivedContent = new byte[allocatedSize];
        do
        {
            bytesRead += socket.getInputStream().read(receivedContent);
        }
        while (bytesRead < contentSize);

        return receivedContent;
    }

    public void write() throws IOException
    {
        if (!socket.isConnected())
        {
            throw new IllegalStateException("Client socket not connected");
        }
        socket.getOutputStream().write(new byte[10]);
    }

    @Override
    public void close()
    {
        Resources.close(socket);
    }
}
