package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;


import static com.michaelszymczak.sample.sockets.nonblockingimpl.OutgoingStream.State.ALL_DATA_SENT;
import static com.michaelszymczak.sample.sockets.nonblockingimpl.OutgoingStream.State.DATA_BUFFERED;

public class OutgoingStream
{
    private final ByteBuffer buffer;
    private final SingleConnectionEvents events;

    private State state;
    private long totalBytesSent;
    private long totalBytesBuffered;

    OutgoingStream(final SingleConnectionEvents events, final int bufferSize)
    {
        this.buffer = ByteBuffer.allocate(bufferSize * 2);
        this.events = events;
        this.state = State.ALL_DATA_SENT;
    }

    void sendData(final WritableByteChannel channel, final ByteBuffer newDataToSend, final long commandId) throws IOException
    {
        if (state == ALL_DATA_SENT)
        {
            final int bytesSent = sendNewData(0, newDataToSend, channel);
            events.dataSent(bytesSent, totalBytesSent, totalBytesBuffered, commandId);
        }
        else if (state == DATA_BUFFERED)
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
        state = newBytesUnsent > 0 ? DATA_BUFFERED : ALL_DATA_SENT;
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

    public enum State
    {
        ALL_DATA_SENT, DATA_BUFFERED
    }
}
