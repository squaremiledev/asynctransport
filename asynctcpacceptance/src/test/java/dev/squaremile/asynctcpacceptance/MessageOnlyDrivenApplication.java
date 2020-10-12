package dev.squaremile.asynctcpacceptance;

import org.agrona.DirectBuffer;


import dev.squaremile.asynctcp.serialization.internal.MessageDrivenApplication;
import dev.squaremile.asynctcp.serialization.internal.TransportEventsDeserialization;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.Event;

public class MessageOnlyDrivenApplication implements MessageDrivenApplication
{
    private final EventDrivenApplication application;
    private final TransportEventsDeserialization deserialization;

    public MessageOnlyDrivenApplication(final EventDrivenApplication application)
    {
        this.application = application;
        this.deserialization = new TransportEventsDeserialization(application::onEvent);
    }

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        deserialization.onSerialized(sourceBuffer, sourceOffset, length);
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
        application.work();
    }
}
