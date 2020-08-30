package dev.squaremile.asynctcp.application;

import java.io.IOException;

import org.agrona.collections.MutableReference;


import dev.squaremile.asynctcp.domain.api.Transport;
import dev.squaremile.asynctcp.domain.api.events.EventListener;
import dev.squaremile.asynctcp.nonblockingimpl.NonBlockingTransport;

public class TransportAppLauncher
{
    public TransportApplication launch(ApplicationFactory applicationFactory) throws IOException
    {
        MutableReference<EventListener> listener = new MutableReference<>();
        Transport transport = new NonBlockingTransport(event -> listener.get().onEvent(event));
        Application app = applicationFactory.create(transport);
        listener.set(app);
        return new TransportApplication(transport, app);
    }
}
