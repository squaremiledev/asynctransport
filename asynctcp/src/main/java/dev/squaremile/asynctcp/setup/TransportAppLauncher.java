package dev.squaremile.asynctcp.setup;

import java.io.IOException;

import org.agrona.collections.MutableReference;


import dev.squaremile.asynctcp.api.app.Application;
import dev.squaremile.asynctcp.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.api.app.EventListener;
import dev.squaremile.asynctcp.api.app.Transport;
import dev.squaremile.asynctcp.internal.nonblockingimpl.NonBlockingTransport;

public class TransportAppLauncher
{
    public TransportApplication launch(ApplicationFactory applicationFactory, final String role)
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
