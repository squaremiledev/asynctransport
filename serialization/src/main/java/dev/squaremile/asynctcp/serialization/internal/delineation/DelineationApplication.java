package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.collections.Long2ObjectHashMap;


import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportCommandHandler;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.values.PredefinedTransportDelineation;

public class DelineationApplication implements Application, TransportCommandHandler
{
    private final Application delegate;
    private final Long2ObjectHashMap<SingleByte> delineationPerConnection = new Long2ObjectHashMap<>();

    public DelineationApplication(final Application delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void onStart()
    {
        delegate.onStart();
    }

    @Override
    public void onStop()
    {
        delegate.onStop();
    }

    @Override
    public void work()
    {
        delegate.work();
    }

    @Override
    public void onEvent(final Event event)
    {
        if (event instanceof Connected)
        {
            Connected connected = (Connected)event;
            delineationPerConnection.put(connected.connectionId(), new SingleByte(delegate::onEvent));
        }
        if (event instanceof ConnectionAccepted)
        {
            ConnectionAccepted connectionAccepted = (ConnectionAccepted)event;
            delineationPerConnection.put(connectionAccepted.connectionId(), new SingleByte(delegate::onEvent));
        }
        if (event instanceof DataReceived)
        {
            DataReceived dataReceived = (DataReceived)event;
            delineationPerConnection.get(dataReceived.connectionId()).delineate(dataReceived);
        }
        else
        {
            delegate.onEvent(event);
        }
    }

    @Override
    public void handle(final TransportCommand command)
    {
        if (command instanceof Listen)
        {
            Listen listen = (Listen)command;
            validateDelineation(listen.delineationName());
        }
        else if (command instanceof Connect)
        {
            Connect connect = (Connect)command;
            validateDelineation(connect.delineationName());
        }
    }

    private void validateDelineation(final String s)
    {
        if (!PredefinedTransportDelineation.SINGLE_BYTE.name().equals(s))
        {
            throw new IllegalArgumentException(s + " is not supported yet");
        }
    }
}
