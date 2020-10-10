package dev.squaremile.asynctcpacceptance.demo;

import java.util.function.Consumer;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public class LoggingConnectedDemoActor implements ConnectionApplication
{
    private final String actor;
    private final Consumer<String> log;

    public LoggingConnectedDemoActor(final String actor, final ConnectionTransport connectionTransport, final ConnectionId connectionId, final Consumer<String> log)
    {
        this.actor = actor;
        this.log = log;
        log.accept(actor + ": creating connected to another actor via TCP using " + connectionId);
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        log.accept(actor + ": <- " + event);
    }

    @Override
    public void onStart()
    {
        log.accept(actor + ": onStart()");
    }

    @Override
    public void onStop()
    {
        log.accept(actor + ": onStop()");
    }

    @Override
    public void work()
    {

    }
}
