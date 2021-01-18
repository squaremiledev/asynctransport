package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.nio.ByteBuffer;


import dev.squaremile.asynctcp.api.transport.app.OnDuty;
import dev.squaremile.asynctcp.api.transport.values.CommandId;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionState;

import static dev.squaremile.asynctcp.internal.transport.nonblockingimpl.SocketProtection.socketCoolDownNs;

public class BufferedOutgoingStream implements OnDuty
{
    private final ByteBuffer buffer;
    private final OutgoingStream outgoingStream;
    private final RelativeClock relativeClock;

    private long nextSendingSlot;
    private boolean requestedToSendData = false;
    private long lastCommandId = CommandId.NO_COMMAND_ID;

    long socketProtectionSendDataRequestCount = 0;
    long socketProtectionSendDataRequestCountResetNs = 0;

    BufferedOutgoingStream(final OutgoingStream outgoingStream, final RelativeClock relativeClock, final int bufferSize)
    {
        this.buffer = ByteBuffer.allocate(bufferSize);
        this.outgoingStream = outgoingStream;
        this.relativeClock = relativeClock;
    }

    void sendData(final ByteBuffer newDataToSend, final long commandId)
    {
        socketProtectionSendDataRequestCount++;
        requestedToSendData = true;
        lastCommandId = commandId;
        buffer.put(newDataToSend);
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

        buffer.flip();
        outgoingStream.sendData(buffer, lastCommandId);
        buffer.clear();
        requestedToSendData = false;
        final long nowNs = relativeClock.relativeNanoTime();
        nextSendingSlot = nowNs + socketCoolDownNs(socketProtectionSendDataRequestCountResetNs, nowNs, socketProtectionSendDataRequestCount);
        socketProtectionSendDataRequestCount = 0;
        socketProtectionSendDataRequestCountResetNs = nowNs;
    }
}
