package dev.squaremile.asynctcp.serialization.internal.messaging;

import org.agrona.concurrent.MessageHandler;


import dev.squaremile.asynctcp.serialization.internal.TransportEventsDeserialization;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.EventListener;

public class SerializedEventDrivenApplication implements EventDrivenApplication
{
    private final EventDrivenApplication application;
    private final SerializedEventSupplier eventSupplier;
    private final MessageHandler messageHandler;

    public SerializedEventDrivenApplication(final EventListener eventListener, final EventDrivenApplication application, final SerializedEventSupplier eventSupplier)
    {
        this.application = application;
        this.eventSupplier = eventSupplier;
        final TransportEventsDeserialization serializedMessageListener = new TransportEventsDeserialization(
                event ->
                {
                    eventListener.onEvent(event);
                    application.onEvent(event);
                });
        this.messageHandler = (msgTypeId, buffer, index, length) -> serializedMessageListener.onSerialized(buffer, index, length);
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
        eventSupplier.poll(messageHandler);
        application.work();
    }
}
