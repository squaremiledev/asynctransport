package dev.squaremile.asynctcp.internal.nonblockingimpl;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


import dev.squaremile.asynctcp.api.commands.Connect;

public class ConnectedNotification
{
    final long connectionId;
    final SocketChannel socketChannel;
    final long commandId;
    final int port;
    final int remotePort;
    final String remoteHost;
    final long deadlineMs;
    final SelectionKey selectionKey;
    final Connect command;
    public String protocolName;

    public ConnectedNotification(long connectionId, SocketChannel socketChannel, Connect command, final long deadlineMs, final SelectionKey selectionKey, final String protocolName)
    {
        this.command = new Connect().set(command.remoteHost(), command.remotePort(), command.commandId(), 1_000);
        this.connectionId = connectionId;
        this.socketChannel = socketChannel;
        this.commandId = command.commandId();
        this.port = command.port();
        this.remotePort = command.remotePort();
        this.remoteHost = command.remoteHost();
        this.deadlineMs = deadlineMs;
        this.selectionKey = selectionKey;
        this.protocolName = protocolName;
    }
}
