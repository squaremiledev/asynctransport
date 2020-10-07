package dev.squaremile.asynctcp.internal;

import java.io.IOException;


import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.serialization.internal.delineation.DelineationApplication;
import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.internal.nonblockingimpl.NonBlockingTransport;

import static dev.squaremile.asynctcp.transport.api.app.TransportCommandHandler.NO_HANDLER;

public class NonProdGradeTransportAppFactory implements TransportApplicationFactory
{
    @Override
    public Application create(final String role, ApplicationFactory applicationFactory)
    {
        try
        {
            ListeningApplication listeningApplication = new ListeningApplication();
            Transport transport = new NonBlockingTransport(listeningApplication, NO_HANDLER, System::currentTimeMillis, role);
            Application app = new DelineationApplication(applicationFactory.create(transport));
            listeningApplication.set(app);
            return new TransportPoweredApplication(transport, app);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static class ListeningApplication implements EventListener
    {
        private Application listeningApplication;

        @Override
        public void onEvent(final Event event)
        {
            listeningApplication.onEvent(event);
        }

        public void set(final Application app)
        {
            listeningApplication = app;
        }

        @Override
        public String toString()
        {
            return "ListeningApplication{" +
                   "listeningApplication=" + listeningApplication +
                   '}';
        }
    }
}
