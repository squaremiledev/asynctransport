package dev.squaremile.asynctcp.transport.api.commands;

import java.nio.ByteBuffer;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.values.CommandId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

public class SendMessage implements ConnectionUserCommand
{
    private final ConnectionId connectionId;
    private final ByteBuffer data;
    private final MutableDirectBuffer buffer;
    private final int initialOffset;
    private final Delineation delineation;
    private final int capacity;
    private int offset;
    private int lengthToCommit;
    private int length;
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
        this.length = 0;
        this.lengthToCommit = 0;
        this.commandId = CommandId.NO_COMMAND_ID;
        this.initialOffset = 0;
        this.offset = 0;
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

    public MutableDirectBuffer prepare(final int length)
    {
        if (delineation.type() == Delineation.Type.FIXED_LENGTH || delineation.type() == Delineation.Type.ASCII_PATTERN)
        {
            this.lengthToCommit = length;
        }
        else
        {
            offset = initialOffset + delineation.padding();
            delineation.type().writeLength(buffer, offset, length - delineation.extraLength());
            offset += delineation.type().lengthFieldLength;
            lengthToCommit = delineation.padding() + delineation.type().lengthFieldLength + length;
        }

        this.length = 0;
        return buffer;
    }

    public int offset()
    {
        return offset;
    }

    public SendMessage reset()
    {
        commandId = CommandId.NO_COMMAND_ID;
        lengthToCommit = 0;
        length = 0;
        offset = initialOffset;
        return this;
    }

    public DirectBuffer buffer()
    {
        return buffer;
    }

    public int length()
    {
        return length;
    }

    public SendMessage commandId(long commandId)
    {
        this.commandId = commandId;
        return this;
    }


    @Override
    public String toString()
    {
        return "SendMessage{" +
               "connectionId=" + connectionId +
               ", data=" + data +
               ", buffer=" + buffer +
               ", offset=" + offset +
               ", delineation=" + delineation +
               ", capacity=" + capacity +
               ", lengthToCommit=" + lengthToCommit +
               ", length=" + length +
               ", commandId=" + commandId +
               '}';
    }

    @Override
    public SendMessage copy()
    {
        SendMessage copy = new SendMessage(connectionId.port(), connectionId.connectionId(), capacity, delineation);
        buffer.getBytes(offset, copy.prepare(length), copy.offset(), length);
        copy.commit();
        return copy;
    }

    public SendMessage commit()
    {
        this.length = lengthToCommit;
        this.lengthToCommit = 0;
        this.offset = initialOffset;
        return this;
    }

    public ByteBuffer data()
    {
        data.position(0).limit(length);
        return data;
    }
}
