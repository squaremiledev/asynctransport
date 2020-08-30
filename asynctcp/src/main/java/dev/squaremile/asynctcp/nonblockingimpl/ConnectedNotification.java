package dev.squaremile.asynctcp.nonblockingimpl;

import java.nio.channels.SocketChannel;


import dev.squaremile.asynctcp.domain.api.commands.Connect;

public class ConnectedNotification
{
    final long connectionId;
    final SocketChannel socketChannel;
    final long commandId;
    final int port;
    final int remotePort;

    public ConnectedNotification(long connectionId, SocketChannel socketChannel, Connect command)
    {
        this.connectionId = connectionId;
        this.socketChannel = socketChannel;
        this.commandId = command.commandId();
        this.port = command.port();
        this.remotePort = command.remotePort();
    }
}
