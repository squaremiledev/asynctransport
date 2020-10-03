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
import dev.squaremile.asynctcp.transport.api.events.StoppedListening;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

public class DelineationApplication implements Application, TransportCommandHandler
{

    private final Application delegate;
    private final Long2ObjectHashMap<DelineationHandler> delineationPerConnection = new Long2ObjectHashMap<>();
    private final DelineationImplementations delineationImplementations = new DelineationImplementations();
    private final Int2ObjectHashMap<String> delineationTypePerListeningPort = new Int2ObjectHashMap<>();
    private final Long2ObjectHashMap<String> delineationTypePerConnectingCommandId = new Long2ObjectHashMap<>();

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
                    delineationImplementations.create(
                            delineationTypePerConnectingCommandId.remove(connected.commandId()),
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
                    delineationImplementations.create(
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
            validateDelineation(listen.delineationName());
            delineationTypePerListeningPort.put(listen.port(), listen.delineationName());
        }
        else if (command instanceof Connect)
        {
            Connect connect = (Connect)command;
            validateDelineation(connect.delineationName());
            delineationTypePerConnectingCommandId.put(connect.commandId(), connect.delineationName());
        }
    }

    private void validateDelineation(final String delineation)
    {
        if (!delineationImplementations.isSupported(delineation))
        {
            throw new IllegalArgumentException(delineation + " is not supported yet");
        }
    }
}
