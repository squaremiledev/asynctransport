package com.michaelszymczak.sample.sockets.nonblockingimpl;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.michaelszymczak.sample.sockets.connection.Channel;


import static com.michaelszymczak.sample.sockets.nonblockingimpl.OutgoingStream.State.ALL_DATA_SENT;
import static com.michaelszymczak.sample.sockets.nonblockingimpl.OutgoingStream.State.DATA_BUFFERED;

public class OutgoingStream
{
    private final ByteBuffer waitingToBeSentBuffer;
    private final ThisConnectionEvents thisConnectionEvents;

    private State state;
    private long totalBytesSent;
    private long totalBytesBuffered;

    OutgoingStream(final int bufferSize, final ThisConnectionEvents thisConnectionEvents)
    {
        this.waitingToBeSentBuffer = ByteBuffer.allocate(bufferSize);
        this.thisConnectionEvents = thisConnectionEvents;
        this.state = State.ALL_DATA_SENT;
    }

    void sendData(final Channel channel, final ByteBuffer newDataToSend, final long commandId) throws IOException
    {
        if (state == ALL_DATA_SENT)
        {
            final int bytesSent = sendNewData(0, newDataToSend, channel);
            thisConnectionEvents.dataSent(bytesSent, totalBytesSent, totalBytesBuffered, commandId);
        }
        else if (state == DATA_BUFFERED)
        {
            waitingToBeSentBuffer.flip();
            final int bufferedDataSentResult = channel.write(waitingToBeSentBuffer);
            final int bufferedBytesSent = bufferedDataSentResult >= 0 ? bufferedDataSentResult : 0;
            final boolean hasSentAllBufferedData = waitingToBeSentBuffer.remaining() == 0;
            waitingToBeSentBuffer.compact();

            if (hasSentAllBufferedData)
            {
                final int bytesSent = sendNewData(bufferedBytesSent, newDataToSend, channel);
                thisConnectionEvents.dataSent(bytesSent, totalBytesSent, totalBytesBuffered, commandId);
            }
            else
            {
                final int newBytesUnsent = newDataToSend.remaining();
                if (newBytesUnsent > 0)
                {
                    waitingToBeSentBuffer.put(newDataToSend);
                }

                totalBytesBuffered += newBytesUnsent;
                totalBytesSent += bufferedBytesSent;
                thisConnectionEvents.dataSent(bufferedBytesSent, totalBytesSent, totalBytesBuffered, commandId);
            }
        }
    }

    private int sendNewData(final int bufferedBytesSent, final ByteBuffer newDataToSend, final Channel channel) throws IOException
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
            waitingToBeSentBuffer.put(newDataToSend);
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
               "waitingToBeSentBuffer=" + waitingToBeSentBuffer +
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
