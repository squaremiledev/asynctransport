package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.collections.Long2ObjectHashMap;


import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportCommandHandler;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.events.StoppedListening;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.api.values.DelineationType;

public class DelineationApplication implements Application, TransportCommandHandler
{

    private final Application delegate;
    private final Long2ObjectHashMap<DelineationHandler> delineationPerConnection = new Long2ObjectHashMap<>();
    private final DelineationHandlerFactory delineationHandlerFactory = new DelineationHandlerFactory();
    private final Int2ObjectHashMap<DelineationType> delineationTypePerListeningPort = new Int2ObjectHashMap<>();

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
        if (event instanceof StartedListening)
        {
            StartedListening startedListening = (StartedListening)event;
            delineationTypePerListeningPort.put(startedListening.port(), startedListening.delineation());
        }
        if (event instanceof DataReceived)
        {
            DataReceived dataReceived = (DataReceived)event;
            if (delineationPerConnection.containsKey(dataReceived.connectionId()))
            {
                delineationPerConnection.get(dataReceived.connectionId()).onData(dataReceived.buffer(), dataReceived.offset(), dataReceived.length());
            }
        }
        else if (event instanceof StoppedListening)
        {
            StoppedListening stoppedListening = (StoppedListening)event;
            delineationTypePerListeningPort.remove(stoppedListening.port());
        }
        else if (event instanceof ConnectionResetByPeer)
        {
            ConnectionResetByPeer connectionResetByPeer = (ConnectionResetByPeer)event;
            delineationPerConnection.remove(connectionResetByPeer.connectionId());
        }
        else if (event instanceof ConnectionClosed)
        {
            ConnectionClosed connectionClosed = (ConnectionClosed)event;
            delineationPerConnection.remove(connectionClosed.connectionId());
        }
        else if (event instanceof Connected)
        {
            Connected connected = (Connected)event;
            final MessageReceived messageReceived = new MessageReceived();
            final ConnectionIdValue connectionIdValue = new ConnectionIdValue(connected);
            delineationPerConnection.put(
                    connectionIdValue.connectionId(),
                    delineationHandlerFactory.create(
                            connected.delineation(),
                            (buffer, offset, length) -> delegate.onEvent(messageReceived.set(connectionIdValue, buffer, offset, length))
                    )
            );
        }
        else if (event instanceof ConnectionAccepted)
        {
            ConnectionAccepted connectionAccepted = (ConnectionAccepted)event;
            if (!delineationTypePerListeningPort.containsKey(connectionAccepted.port()))
            {
                return;
            }
            final MessageReceived messageReceived = new MessageReceived();
            final ConnectionIdValue connectionIdValue = new ConnectionIdValue(connectionAccepted);
            delineationPerConnection.put(
                    connectionIdValue.connectionId(),
                    delineationHandlerFactory.create(
                            delineationTypePerListeningPort.get(connectionAccepted.port()),
                            (buffer, offset, length) -> delegate.onEvent(messageReceived.set(connectionIdValue, buffer, offset, length))
                    )
            );
        }

        if (!(event instanceof DataReceived))
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
            validateDelineation(listen.delineation());
        }
        else if (command instanceof Connect)
        {
            Connect connect = (Connect)command;
            validateDelineation(connect.delineation());
        }
    }

    private void validateDelineation(final DelineationType delineation)
    {
        if (!delineationHandlerFactory.isSupported(delineation))
        {
            throw new IllegalArgumentException(delineation + " is not supported yet");
        }
    }
}
