package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import static org.agrona.LangUtil.rethrowUnchecked;


import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionState;
import dev.squaremile.asynctcp.internal.transport.domain.connection.SingleConnectionEvents;

import static dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionState.DATA_TO_SEND_BUFFERED;
import static dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionState.NO_OUTSTANDING_DATA;
import static java.lang.Math.max;

public class OutgoingStream
{
    private final WritableByteChannel channel;
    private final ByteBuffer buffer;
    private final SingleConnectionEvents events;

    private ConnectionState state;
    private long totalBytesSent;
    private long totalBytesBuffered;

    OutgoingStream(final WritableByteChannel channel, final SingleConnectionEvents events, final int bufferSize)
    {
        this.channel = channel;
        this.buffer = ByteBuffer.allocate(bufferSize);
        this.events = events;
        this.state = ConnectionState.NO_OUTSTANDING_DATA;
    }

    public ConnectionState state()
    {
        return state;
    }

    void sendData(final ByteBuffer newDataToSend, final long commandId)
    {
        try
        {
            switch (state)
            {
                case NO_OUTSTANDING_DATA:
                    int bytesSentA = sendNewData(0, newDataToSend, channel);
                    if (bytesSentA >= 0)
                    {
                        events.onEvent(events.dataSentEvent().set(bytesSentA, totalBytesSent, totalBytesBuffered, commandId));
                    }
                    break;
                case DATA_TO_SEND_BUFFERED:
                    buffer.flip();
                    final int bufferedDataSentResult = channel.write(buffer);
                    final int bufferedBytesSent = max(bufferedDataSentResult, 0);
                    final boolean hasSentAllBufferedData = buffer.remaining() == 0;
                    buffer.compact();

                    if (hasSentAllBufferedData)
                    {
                        int bytesSentB = sendNewData(bufferedBytesSent, newDataToSend, channel);
                        if (bytesSentB >= 0)
                        {
                            events.onEvent(events.dataSentEvent().set(bytesSentB, totalBytesSent, totalBytesBuffered, commandId));
                        }
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
        }
        catch (IOException e)
        {
            // TODO: when reset by peer it's better not to throw as the method does not mention such possibility
            rethrowUnchecked(e);
        }
    }

    private int sendNewData(final int bufferedBytesSent, final ByteBuffer newDataToSend, final WritableByteChannel channel) throws IOException
    {
        int newBytesSent = 0;
        if (newDataToSend.remaining() > 0)
        {
            newBytesSent += Math.max(channel.write(newDataToSend), 0);
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
