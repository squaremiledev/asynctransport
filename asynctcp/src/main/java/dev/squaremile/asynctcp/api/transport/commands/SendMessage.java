package dev.squaremile.asynctcp.api.transport.commands;

import java.nio.ByteBuffer;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;


import dev.squaremile.asynctcp.api.transport.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.api.transport.values.CommandId;
import dev.squaremile.asynctcp.api.transport.values.ConnectionId;
import dev.squaremile.asynctcp.api.transport.values.ConnectionIdValue;
import dev.squaremile.asynctcp.api.transport.values.Delineation;

public class SendMessage implements ConnectionUserCommand
{
    private final ConnectionId connectionId;
    private final ByteBuffer data;
    private final MutableDirectBuffer buffer;
    private final Delineation delineation;
    private final int capacity;
    private int writeOffset;
    private int totalUnsentLength;
    private long commandId;

    public SendMessage(final ConnectionId connectionId, final int capacity, final Delineation delineation)
    {
        this(connectionId.port(), connectionId.connectionId(), capacity, delineation);
    }

    public SendMessage(final int port, final long connectionId, final int capacity, final Delineation delineation)
    {
        this.capacity = capacity;
        this.connectionId = new ConnectionIdValue(port, connectionId);
        this.data = ByteBuffer.allocate(capacity);
        this.buffer = new UnsafeBuffer(data);
        this.totalUnsentLength = 0;
        this.commandId = CommandId.NO_COMMAND_ID;
        this.writeOffset = 0;
        this.delineation = delineation;
    }

    @Override
    public int port()
    {
        return connectionId.port();
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    @Override
    public long connectionId()
    {
        return connectionId.connectionId();
    }

    public MutableDirectBuffer prepareToWrite()
    {
        this.writeOffset = totalUnsentLength + delineation.padding() + delineation.type().lengthFieldLength;
        return buffer;
    }

    public int writeOffset()
    {
        return writeOffset;
    }

    public SendMessage reset()
    {
        commandId = CommandId.NO_COMMAND_ID;
        totalUnsentLength = 0;
        writeOffset = 0;
        return this;
    }

    public MutableDirectBuffer buffer()
    {
        return buffer;
    }

    public int totalUnsentLength()
    {
        return totalUnsentLength;
    }

    public SendMessage commandId(long commandId)
    {
        this.commandId = commandId;
        return this;
    }

    public SendMessage commitWrite(final int length)
    {
        this.delineation.type().writeLength(buffer, this.totalUnsentLength + delineation.padding(), length - delineation.extraLength());
        this.totalUnsentLength += delineation.padding() + delineation.type().lengthFieldLength + length;
        return this;
    }

    @Override
    public SendMessage copy()
    {
        SendMessage copy = new SendMessage(connectionId.port(), connectionId.connectionId(), capacity, delineation);
        buffer.getBytes(0, copy.buffer(), 0, totalUnsentLength);
        copy.set(writeOffset, totalUnsentLength, commandId);
        return copy;
    }

    public ByteBuffer data()
    {
        data.position(0).limit(totalUnsentLength);
        return data;
    }

    public SendMessage set(final int writeOffset, final int totalUnsentLength, final long commandId)
    {
        this.writeOffset = writeOffset;
        this.totalUnsentLength = totalUnsentLength;
        this.commandId = commandId;
        return this;
    }

    public void setTotalUnsentLength(final int totalUnsentLength)
    {
        this.totalUnsentLength = totalUnsentLength;
    }

    @Override
    public String toString()
    {
        return "SendMessage{" +
               "connectionId=" + connectionId +
               ", delineation=" + delineation +
               ", capacity=" + capacity +
               ", writeOffset=" + writeOffset +
               ", totalUnsentLength=" + totalUnsentLength +
               ", commandId=" + commandId +
               '}';
    }
}
