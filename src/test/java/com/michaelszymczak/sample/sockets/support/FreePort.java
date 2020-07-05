package com.michaelszymczak.sample.sockets.support;

import java.io.IOException;
import java.net.ServerSocket;

public class FreePort
{

    public static int freePort(final int port) throws IOException
    {
        try (ServerSocket socket = new ServerSocket(port))
        {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }
}
