package dev.squaremile.asynctcp.application;

import java.io.IOException;

import org.agrona.collections.MutableReference;


import dev.squaremile.asynctcp.domain.api.Transport;
import dev.squaremile.asynctcp.domain.api.events.EventListener;
import dev.squaremile.asynctcp.nonblockingimpl.CommandsQueueTransport;
import dev.squaremile.asynctcp.nonblockingimpl.NonBlockingTransport;

public class TransportAppLauncher
{
    public TransportApplication launch(ApplicationFactory applicationFactory, final String role)
    {
        MutableReference<EventListener> listener = new MutableReference<>();
        try
        {
            Transport transport = new CommandsQueueTransport(new NonBlockingTransport(event -> listener.get().onEvent(event), System::currentTimeMillis, role));
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
