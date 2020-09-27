package dev.squaremile.asynctcp.transport.testfixtures.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import org.agrona.CloseHelper;

import static org.agrona.LangUtil.rethrowUnchecked;

public class SampleClient implements AutoCloseable
{

    private final Socket socket;
    private final int timeoutMs;

    public SampleClient()
    {
        this.timeoutMs = 1000;
        this.socket = new Socket();
        try
        {
            this.socket.setSoTimeout(timeoutMs);
        }
        catch (final SocketException e)
        {
            rethrowUnchecked(e);
        }
    }

    public SampleClient connectedTo(final int port)
    {
        return connectedTo(port, -1);
    }

    public SampleClient connectedTo(final int port, final int localPort)
    {
        try
        {
            if (localPort != -1)
            {
                socket.bind(new InetSocketAddress("127.0.0.1", localPort));
            }
            socket.connect(new InetSocketAddress("127.0.0.1", port), timeoutMs);
        }
        catch (Exception e)
        {
            rethrowUnchecked(e);
        }

        return this;
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

    public boolean hasServerClosedConnection()
    {
        try
        {
            return socket.getInputStream().read() == -1;
        }
        catch (SocketException e)
        {
            if ("Socket is closed".equalsIgnoreCase(e.getMessage()))
            {
                return true;
            }
            rethrowUnchecked(e);
        }
        catch (IOException e)
        {
            rethrowUnchecked(e);
        }
        return false;
    }

    @Override
    public void close()
    {
        CloseHelper.close(socket);
    }

    public interface ReadDataConsumer
    {
        ReadDataConsumer DEV_NULL = (data, length) ->
        {

        };

        void consume(byte[] data, int length);
    }
}
