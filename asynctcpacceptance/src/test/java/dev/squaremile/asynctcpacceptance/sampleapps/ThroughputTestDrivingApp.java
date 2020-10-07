package dev.squaremile.asynctcpacceptance.sampleapps;

import org.agrona.collections.MutableLong;
import org.agrona.collections.MutableReference;


import dev.squaremile.asynctcp.serialization.internal.delineation.DelineationApplication;
import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.DelineationType;

class ThroughputTestDrivingApp implements ApplicationFactory
{
    private final MutableReference<Connected> connectedEventHolder = new MutableReference<>();
    private final MutableLong totalBytesReceived = new MutableLong(0);
    private final int port;
    private Transport transport;
    private App app;
    private DelineationType delineation;

    public ThroughputTestDrivingApp(final int port, final DelineationType delineation)
    {
        this.port = port;
        this.delineation = delineation;
    }


    @Override
    public Application create(final Transport transport)
    {
        this.transport = transport;
        app = new App(transport);
        return app;
    }

    public Transport transport()
    {
        return transport;
    }

    public App app()
    {
        return app;
    }

    public Connected connectedEvent()
    {
        return connectedEventHolder.get();
    }

    public long totalBytesReceived()
    {
        return totalBytesReceived.get();
    }

    class App implements Application
    {
        private final Transport transport;

        public App(final Transport transport)
        {
            this.transport = transport;
        }

        @Override
        public void onStart()
        {
            transport.handle(transport.command(Connect.class).set("localhost", port, (long)1, 50, delineation));
        }

        @Override
        public void work()
        {
            transport.work();
        }

        @Override
        public void onEvent(final Event event)
        {
            if (event instanceof Connected)
            {
                Connected connectedEvent = (Connected)event;
                connectedEventHolder.set(connectedEvent.copy());
            }
            else if (event instanceof DataReceived)
            {
                DataReceived dataReceivedEvent = (DataReceived)event;
                totalBytesReceived.set(dataReceivedEvent.totalBytesReceived());
            }
            else if (event instanceof MessageReceived)
            {
                MessageReceived dataReceivedEvent = (MessageReceived)event;
                totalBytesReceived.addAndGet(dataReceivedEvent.length());
            }
        }
    }
}
