package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import com.michaelszymczak.sample.sockets.connection.ConnectionState;


import static com.michaelszymczak.sample.sockets.connection.ConnectionState.DATA_TO_SEND_BUFFERED;
import static com.michaelszymczak.sample.sockets.connection.ConnectionState.NO_OUTSTANDING_DATA;

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
        if (state == NO_OUTSTANDING_DATA)
        {
            final int bytesSent = sendNewData(0, newDataToSend, channel);
            events.dataSent(bytesSent, totalBytesSent, totalBytesBuffered, commandId);
        }
        else if (state == DATA_TO_SEND_BUFFERED)
        {
            buffer.flip();
            final int bufferedDataSentResult = channel.write(buffer);
            final int bufferedBytesSent = bufferedDataSentResult >= 0 ? bufferedDataSentResult : 0;
            final boolean hasSentAllBufferedData = buffer.remaining() == 0;
            buffer.compact();

            if (hasSentAllBufferedData)
            {
                final int bytesSent = sendNewData(bufferedBytesSent, newDataToSend, channel);
                events.dataSent(bytesSent, totalBytesSent, totalBytesBuffered, commandId);
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
                events.dataSent(bufferedBytesSent, totalBytesSent, totalBytesBuffered, commandId);
            }
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
