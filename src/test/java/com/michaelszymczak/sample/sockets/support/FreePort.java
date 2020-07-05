package com.michaelszymczak.sample.sockets.support;

import java.io.IOException;
import java.net.ServerSocket;

public class FreePort
{
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

    public static int freePortOtherThan(final int reservedPort)
    {
        int newPort;
        do
        {
            newPort = freePort(0);
        }
        while (newPort != reservedPort);
        return newPort;
    }
}
