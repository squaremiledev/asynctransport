package dev.squaremile.asynctcp.nonblockingimpl;

import java.nio.channels.SocketChannel;

public class ConnectedNotification
{
    final long connectionId;
    final SocketChannel socketChannel;
    final long commandId;

    public ConnectedNotification(long connectionId, SocketChannel socketChannel, long commandId)
    {
        this.connectionId = connectionId;
        this.socketChannel = socketChannel;
        this.commandId = commandId;
    }
}
