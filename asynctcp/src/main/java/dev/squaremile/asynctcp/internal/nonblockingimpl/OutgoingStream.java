package dev.squaremile.asynctcp.internal.nonblockingimpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;


import dev.squaremile.asynctcp.internal.domain.connection.ConnectionState;
import dev.squaremile.asynctcp.internal.domain.connection.SingleConnectionEvents;

import static dev.squaremile.asynctcp.internal.domain.connection.ConnectionState.DATA_TO_SEND_BUFFERED;
import static dev.squaremile.asynctcp.internal.domain.connection.ConnectionState.NO_OUTSTANDING_DATA;

public class OutgoingStream
{
    private final ByteBuffer buffer;
    private final SingleConnectionEvents events;

    private ConnectionState state;
    private long totalBytesSent;
    private long totalBytesBuffered;

    OutgoingStream(final SingleConnectionEvents events, final int bufferSize)
    {
        this.buffer = ByteBuffer.allocate(bufferSize * 2);
        this.events = events;
        this.state = ConnectionState.NO_OUTSTANDING_DATA;
    }

    public ConnectionState state()
    {
        return state;
    }

    ConnectionState sendData(final WritableByteChannel channel, final ByteBuffer newDataToSend, final long commandId) throws IOException
    {
        switch (state)
        {
            case NO_OUTSTANDING_DATA:
                events.onEvent(events.dataSentEvent().set(sendNewData(0, newDataToSend, channel), totalBytesSent, totalBytesBuffered, commandId));
                break;
            case DATA_TO_SEND_BUFFERED:
                buffer.flip();
                final int bufferedDataSentResult = channel.write(buffer);
                final int bufferedBytesSent = bufferedDataSentResult >= 0 ? bufferedDataSentResult : 0;
                final boolean hasSentAllBufferedData = buffer.remaining() == 0;
                buffer.compact();

                if (hasSentAllBufferedData)
                {
                    events.onEvent(events.dataSentEvent().set(sendNewData(bufferedBytesSent, newDataToSend, channel), totalBytesSent, totalBytesBuffered, commandId));
                }
                else
                {
                    final int newBytesUnsent = newDataToSend.remaining();
                    if (newBytesUnsent > 0)
                    {
                        buffer.put(newDataToSend);
                    }

                    totalBytesBuffered += newBytesUnsent;
                    totalBytesSent += bufferedBytesSent;
                    events.onEvent(events.dataSentEvent().set(bufferedBytesSent, totalBytesSent, totalBytesBuffered, commandId));
                }
                break;
            case CLOSED:
                break;
        }
        return state;
    }

    private int sendNewData(final int bufferedBytesSent, final ByteBuffer newDataToSend, final WritableByteChannel channel) throws IOException
    {
        int newBytesSent = 0;
        if (newDataToSend.remaining() > 0)
        {
            final int newDataSentResult = channel.write(newDataToSend);
            newBytesSent += newDataSentResult >= 0 ? newDataSentResult : 0;
        }
        final int newBytesUnsent = newDataToSend.remaining();
        if (newBytesUnsent > 0)
        {
            buffer.put(newDataToSend);
        }

        final int bytesSent = bufferedBytesSent + newBytesSent;
        totalBytesBuffered += newBytesSent + newBytesUnsent;
        totalBytesSent += bytesSent;
        state = newBytesUnsent > 0 ? DATA_TO_SEND_BUFFERED : NO_OUTSTANDING_DATA;
        return bytesSent;
    }

    @Override
    public String toString()
    {
        return "OutgoingStream{" +
               "buffer=" + buffer +
               ", state=" + state +
               ", totalBytesSent=" + totalBytesSent +
               ", totalBytesBuffered=" + totalBytesBuffered +
               '}';
    }

}
