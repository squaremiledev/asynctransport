package dev.squaremile.asynctcp.serialization.internal.messaging;

import org.agrona.concurrent.MessageHandler;


import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.serialization.internal.TransportEventsDeserialization;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;

public class SerializedEventDrivenApplication implements TransportApplicationOnDuty
{
    private final TransportApplicationOnDuty application;
    private final SerializedEventSupplier eventSupplier;
    private final MessageHandler messageHandler;

    public SerializedEventDrivenApplication(
            final EventListener eventListener,
            final TransportApplicationOnDuty application,
            final SerializedEventSupplier eventSupplier,
            final SerializedEventListener serializedEventListener
    )
    {
        this.application = application;
        this.eventSupplier = eventSupplier;
        final SerializedEventListener serializedEventsListener = new TransportEventsDeserialization(
                event ->
                {
                    eventListener.onEvent(event);
                    application.onEvent(event);
                });
        this.messageHandler = (msgTypeId, buffer, index, length) ->
        {
            serializedEventListener.onSerialized(buffer, index, length);
            serializedEventsListener.onSerialized(buffer, index, length);
        };
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
