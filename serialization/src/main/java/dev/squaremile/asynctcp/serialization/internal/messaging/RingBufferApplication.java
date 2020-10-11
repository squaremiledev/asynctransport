package dev.squaremile.asynctcp.serialization.internal.messaging;

import org.agrona.concurrent.ringbuffer.RingBuffer;


import dev.squaremile.asynctcp.serialization.internal.TransportEventsDeserialization;
import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;

public class RingBufferApplication implements Application
{
    private final Application application;
    private final RingBufferReader ringBufferReader;

    public RingBufferApplication(final EventListener eventListener, final Application application, final RingBuffer ringBuffer)
    {
        this.application = application;
        this.ringBufferReader = new RingBufferReader("fromNetwork", ringBuffer, new TransportEventsDeserialization(
                event ->
                {
                    eventListener.onEvent(event);
                    application.onEvent(event);
                }));
    }

    @Override
    public void onEvent(final Event event)
    {
        throw new UnsupportedOperationException("There should be no need to send events to the app directly");
    }

    @Override
    public void onStart()
    {
        application.onStart();
    }

    @Override
    public void onStop()
    {
        application.onStop();
    }

    @Override
    public void work()
    {
        ringBufferReader.read();
        application.work();
    }
}
