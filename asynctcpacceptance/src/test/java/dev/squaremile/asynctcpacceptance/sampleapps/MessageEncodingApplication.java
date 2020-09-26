package dev.squaremile.asynctcpacceptance.sampleapps;

import java.nio.ByteBuffer;


import dev.squaremile.asynctcp.api.app.Application;
import dev.squaremile.asynctcp.api.app.Event;
import dev.squaremile.asynctcp.api.events.DataReceived;
import dev.squaremile.asynctcp.api.events.MessageReceived;
import dev.squaremile.asynctcp.api.values.PredefinedTransportEncoding;

public class MessageEncodingApplication implements Application
{
    private final Application delegate;
    private final MessageReceived messageReceivedFlyweight = new MessageReceived();
    // TODO: [perf] use offset and the underlying buffer instead
    private final byte[] oneByteFlyweight = new byte[1];
    private final ByteBuffer oneByteByteBuffer = ByteBuffer.wrap(oneByteFlyweight);

    public MessageEncodingApplication(final Application delegate, final PredefinedTransportEncoding encoding)
    {
        if (encoding != PredefinedTransportEncoding.SINGLE_BYTE)
        {
            throw new IllegalArgumentException(encoding + " is not supported yet");
        }
        this.delegate = delegate;
    }

    @Override
    public void onStart()
    {
        delegate.onStart();
    }

    @Override
    public void onStop()
    {
        delegate.onStop();
    }

    @Override
    public void work()
    {
        delegate.work();
    }

    @Override
    public void onEvent(final Event event)
    {
        if (event instanceof DataReceived)
        {
            DataReceived dataReceived = (DataReceived)event;
            int length = dataReceived.length();
            for (int i = 0; i < length; i++)
            {
                oneByteFlyweight[0] = dataReceived.data().get(i);
                delegate.onEvent(messageReceivedFlyweight.set(dataReceived, oneByteByteBuffer, 1));
            }
        }
        else
        {
            delegate.onEvent(event);
        }
    }
}
