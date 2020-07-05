package com.michaelszymczak.sample.sockets.support;

import java.io.IOException;
import java.net.ServerSocket;

class FreePort
{
    static int freePort(final int port)
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
}
