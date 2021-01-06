package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;


import dev.squaremile.asynctcp.api.transport.app.OnDuty;
import dev.squaremile.asynctcp.api.transport.values.CommandId;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionState;

public class BufferedOutgoingStream implements OnDuty
{
    public static final long NEXT_SENDING_SLOT_DELAY = TimeUnit.MICROSECONDS.toNanos(5);
    private final ByteBuffer buffer;
    private final OutgoingStream outgoingStream;
    private final RelativeClock relativeClock;

    private long nextSendingSlot;
    private boolean requestedToSendData = false;
    private long lastCommandId = CommandId.NO_COMMAND_ID;

    BufferedOutgoingStream(final OutgoingStream outgoingStream, final RelativeClock relativeClock, final int bufferSize)
    {
        this.buffer = ByteBuffer.allocate(bufferSize);
        this.outgoingStream = outgoingStream;
        this.relativeClock = relativeClock;
    }

    void sendData(final ByteBuffer newDataToSend, final long commandId)
    {
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
        if (!requestedToSendData || relativeClock.relativeNanoTime() < nextSendingSlot)
        {
            return;
        }

        buffer.flip();
        outgoingStream.sendData(buffer, lastCommandId);
        buffer.clear();
        requestedToSendData = false;
        nextSendingSlot = relativeClock.relativeNanoTime() + NEXT_SENDING_SLOT_DELAY;
    }
}
