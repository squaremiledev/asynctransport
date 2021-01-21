package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.nio.ByteBuffer;


import dev.squaremile.asynctcp.api.transport.app.OnDuty;
import dev.squaremile.asynctcp.api.transport.commands.SendMessage;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionState;

import static dev.squaremile.asynctcp.internal.transport.nonblockingimpl.SocketProtection.socketCoolDownNs;

public class BufferedOutgoingStream implements OnDuty
{
    private final ByteBuffer buffer;
    private final OutgoingStream outgoingStream;
    private final RelativeClock relativeClock;

    private long nextSendingSlot;
    private boolean requestedToSendData = false;
    private SendMessage lastCommand;
    private long lastCommandId;

    long socketProtectionSendDataRequestCount = 0;
    long socketProtectionSendDataRequestCountResetNs = 0;

    BufferedOutgoingStream(final OutgoingStream outgoingStream, final RelativeClock relativeClock, final int bufferSize)
    {
        this.buffer = ByteBuffer.allocate(bufferSize);
        this.outgoingStream = outgoingStream;
        this.relativeClock = relativeClock;
    }

    public void sendMessage(final SendMessage command)
    {
        socketProtectionSendDataRequestCount++;
        lastCommandId = command.commandId();
        if (command.isExclusivePublication())
        {
            lastCommand = command;
        }
        else
        {
            buffer.put(command.data());
            command.reset();
        }

        requestedToSendData = true;
        work();
    }

    public ConnectionState state()
    {
        return outgoingStream.state();
    }

    @Override
    public void work()
    {
        sendDataIfReady();
    }

    @Override
    public String toString()
    {
        return "BufferedOutgoingStream{" +
               "outgoingStream=" + outgoingStream +
               '}';
    }

    private void sendDataIfReady()
    {
        if (!requestedToSendData)
        {
            return;
        }

        if (relativeClock.relativeNanoTime() < nextSendingSlot)
        {
            return;
        }

        if (lastCommand != null)
        {
            outgoingStream.sendData(lastCommand.data(), lastCommand.commandId());
            lastCommand.reset();
            lastCommand = null;
        }
        else
        {
            buffer.flip();
            outgoingStream.sendData(buffer, lastCommandId);
            buffer.clear();
        }

        requestedToSendData = false;
        final long nowNs = relativeClock.relativeNanoTime();
        nextSendingSlot = nowNs + socketCoolDownNs(socketProtectionSendDataRequestCountResetNs, nowNs, socketProtectionSendDataRequestCount);
        socketProtectionSendDataRequestCount = 0;
        socketProtectionSendDataRequestCountResetNs = nowNs;
    }
}
