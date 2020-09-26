package dev.squaremile.asynctcpacceptance.sampleapps;

import java.nio.ByteBuffer;


import dev.squaremile.asynctcp.api.app.Application;
import dev.squaremile.asynctcp.api.app.Event;
import dev.squaremile.asynctcp.api.events.DataReceived;
import dev.squaremile.asynctcp.api.events.MessageReceived;
import dev.squaremile.asynctcp.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.api.values.PredefinedTransportEncoding;

public class MessageEncodingApplication implements Application
{
    private final Application delegate;
    private final PredefinedTransportEncoding encoding;

    public MessageEncodingApplication(final Application delegate, final PredefinedTransportEncoding encoding)
    {
        if (encoding != PredefinedTransportEncoding.SINGLE_BYTE)
        {
            throw new IllegalArgumentException(encoding + " is not supported yet");
        }
        this.delegate = delegate;
        this.encoding = encoding;
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
            // TODO: implement actual logic
            delegate.onEvent(new MessageReceived(new ConnectionIdValue(8808, 100)).set(ByteBuffer.wrap(new byte[]{5}), 1));
        }
        else
        {
            delegate.onEvent(event);
        }
    }
}
