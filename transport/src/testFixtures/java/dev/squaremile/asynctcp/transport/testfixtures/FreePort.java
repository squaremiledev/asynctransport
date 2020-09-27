package dev.squaremile.asynctcp.transport.testfixtures;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;

public class FreePort
{
    public static int freePort()
    {
        return freePort(0);
    }

    public static int freePort(final int port)
    {
        try (ServerSocket socket = new ServerSocket(port))
        {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static int freePortOtherThan(final Integer... reservedPorts)
    {
        final List<Integer> reserved = Arrays.asList(reservedPorts);
        int newPort;
        do
        {
            newPort = freePort(0);
        }
        while (reserved.contains(newPort));
        return newPort;
    }
}
