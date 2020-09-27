package dev.squaremile.asynctcp.transport.setup;

import java.io.IOException;

import org.agrona.collections.MutableReference;


import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.internal.nonblockingimpl.NonBlockingTransport;

public class TransportAppFactory
{
    public TransportApplication create(final String role, ApplicationFactory applicationFactory)
    {
        MutableReference<EventListener> listener = new MutableReference<>();
        try
        {
            Transport transport = new NonBlockingTransport(event -> listener.get().onEvent(event), System::currentTimeMillis, role);
            Application app = applicationFactory.create(transport);
            listener.set(app);
            return new TransportApplication(transport, app);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
