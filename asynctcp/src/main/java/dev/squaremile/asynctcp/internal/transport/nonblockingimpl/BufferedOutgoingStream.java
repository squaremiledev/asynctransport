package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.nio.ByteBuffer;


import dev.squaremile.asynctcp.api.transport.app.ApplicationLifecycle;
import dev.squaremile.asynctcp.api.transport.app.OnDuty;
import dev.squaremile.asynctcp.api.transport.values.CommandId;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionState;

import static dev.squaremile.asynctcp.internal.transport.nonblockingimpl.SocketProtection.socketCoolDownNs;

public class BufferedOutgoingStream implements ApplicationLifecycle, OnDuty
{
    private final String role;
    private final ByteBuffer buffer;
    private final OutgoingStream outgoingStream;
    private final RelativeClock relativeClock;

    private long socketProtectionSendDataRequestCount = 0;
    private long socketProtectionSendDataRequestCountResetNs = 0;
    private long nextSendingSlot;
    private boolean requestedToSendData = false;
    private long lastCommandId = CommandId.NO_COMMAND_ID;

    BufferedOutgoingStream(
            final String role,
            final OutgoingStream outgoingStream,
            final RelativeClock relativeClock,
            final int bufferSize
    )
    {
        this.role = role;
        this.buffer = ByteBuffer.allocate(bufferSize);
        this.outgoingStream = outgoingStream;
        this.relativeClock = relativeClock;
    }

    String sendData(final ByteBuffer newDataToSend, final long commandId)
    {
        socketProtectionSendDataRequestCount++;
        requestedToSendData = true;
        lastCommandId = commandId;
        buffer.put(newDataToSend);
        return sendDataIfReady();
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
               "role='" + role + '\'' +
               ", buffer=" + buffer +
               ", outgoingStream=" + outgoingStream +
               ", relativeClock=" + relativeClock +
               ", nextSendingSlot=" + nextSendingSlot +
               ", requestedToSendData=" + requestedToSendData +
               ", lastCommandId=" + lastCommandId +
               ", socketProtectionSendDataRequestCount=" + socketProtectionSendDataRequestCount +
               ", socketProtectionSendDataRequestCountResetNs=" + socketProtectionSendDataRequestCountResetNs +
               '}';
    }

    private String sendDataIfReady()
    {
        if (!requestedToSendData)
        {
            return null;
        }

        long nowNsBefore = relativeClock.relativeNanoTime();
        if (nowNsBefore < nextSendingSlot)
        {
            return null;
        }

        buffer.flip();
        final String errorResult = outgoingStream.sendData(buffer, lastCommandId);
        buffer.clear();
        final long nowNs = relativeClock.relativeNanoTime();
        requestedToSendData = false;
        nextSendingSlot = nowNs + socketCoolDownNs(socketProtectionSendDataRequestCountResetNs, nowNs, socketProtectionSendDataRequestCount);
        socketProtectionSendDataRequestCount = 0;
        socketProtectionSendDataRequestCountResetNs = nowNs;
        return errorResult;
    }

    @Override
    public void onStart()
    {

    }

    @Override
    public void onStop()
    {

    }
}
