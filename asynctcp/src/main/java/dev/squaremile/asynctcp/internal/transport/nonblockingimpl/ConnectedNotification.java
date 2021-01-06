package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


import dev.squaremile.asynctcp.api.transport.commands.Connect;
import dev.squaremile.asynctcp.api.transport.values.Delineation;

class ConnectedNotification
{
    final SocketChannel socketChannel;
    final long commandId;
    final int port;
    final String remoteHost;
    final long deadlineMs;
    final SelectionKey selectionKey;
    final Connect command;

    ConnectedNotification(final SocketChannel socketChannel, final Connect command, final long deadlineMs, final SelectionKey selectionKey, final Delineation delineation)
    {
        this.command = new Connect().set(command.remoteHost(), command.remotePort(), command.commandId(), 1_000, delineation);
        this.socketChannel = socketChannel;
        this.commandId = command.commandId();
        this.port = command.port();
        this.remoteHost = command.remoteHost();
        this.deadlineMs = deadlineMs;
        this.selectionKey = selectionKey;
    }
}
