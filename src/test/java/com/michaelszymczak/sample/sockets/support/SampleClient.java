package com.michaelszymczak.sample.sockets.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class SampleClient implements AutoCloseable
{

    private final Socket socket;
    private final int timeoutMs;

    public SampleClient() throws SocketException
    {
        this.timeoutMs = 1000;
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
        return read(contentSize, allocatedSize, ReadDataConsumer.DEV_NULL);
    }

    public byte[] read(final int contentSize, final int allocatedSize, final ReadDataConsumer dataConsumer) throws IOException
    {
        int bytesRead = 0;
        final byte[] receivedContent = new byte[allocatedSize];
        do
        {
            bytesRead += socket.getInputStream().read(receivedContent, bytesRead, receivedContent.length - bytesRead);
        }
        while (bytesRead < contentSize);
        dataConsumer.consume(receivedContent, bytesRead);

        return receivedContent;
    }

    public void write() throws IOException
    {
        write("foo".getBytes(StandardCharsets.US_ASCII));
    }

    public void write(final byte[] content) throws IOException
    {
        socket.getOutputStream().write(content);
    }

    public boolean hasServerClosedConnection() throws IOException
    {
        return socket.getInputStream().read() == -1;
    }

    @Override
    public void close()
    {
        Resources.close(socket);
    }

    public interface ReadDataConsumer
    {
        ReadDataConsumer DEV_NULL = (data, length) ->
        {

        };

        void consume(byte[] data, int length);
    }
}
