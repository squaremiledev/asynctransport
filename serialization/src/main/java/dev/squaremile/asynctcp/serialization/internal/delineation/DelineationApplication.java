package dev.squaremile.asynctcp.serialization.internal.delineation;

import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.values.PredefinedTransportDelineation;

public class DelineationApplication implements Application
{
    private final Application delegate;
    // TODO: [perf] use offset and the underlying buffer instead
    private final SingleByte singleByteDelineation;

    public DelineationApplication(final Application delegate, final PredefinedTransportDelineation delineation)
    {
        if (delineation != PredefinedTransportDelineation.SINGLE_BYTE)
        {
            throw new IllegalArgumentException(delineation + " is not supported yet");
        }
        this.delegate = delegate;
        this.singleByteDelineation = new SingleByte(delegate::onEvent);
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
            singleByteDelineation.delineate((DataReceived)event);
        }
        else
        {
            delegate.onEvent(event);
        }
    }
}
