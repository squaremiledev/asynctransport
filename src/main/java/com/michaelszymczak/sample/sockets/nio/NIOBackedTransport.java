package com.michaelszymczak.sample.sockets.nio;

import java.io.IOException;

import com.michaelszymczak.sample.sockets.api.Transport;
import com.michaelszymczak.sample.sockets.api.TransportEventsListener;
import com.michaelszymczak.sample.sockets.api.commands.CloseConnection;
import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.commands.StopListening;
import com.michaelszymczak.sample.sockets.api.commands.TransportCommand;

public class NIOBackedTransport implements AutoCloseable, Transport, Workman
{
    private final TransportEventsListener transportEventsListener;
    private SocketApi socketApi;

    public NIOBackedTransport(final TransportEventsListener transportEventsListener) throws IOException
    {
        this.socketApi = new SocketApi(transportEventsListener, new ConnectionIdSource());
        this.transportEventsListener = transportEventsListener;
    }

    @Override
    public void handle(final TransportCommand command)
    {
        if (command instanceof Listen)
        {
            final Listen cmd = (Listen)command;
            transportEventsListener.onEvent(socketApi.listen(cmd.port(), cmd.commandId()));
        }
        else if (command instanceof StopListening)
        {
            final StopListening cmd = (StopListening)command;
            transportEventsListener.onEvent(socketApi.stopListening(cmd.port(), cmd.commandId()));
        }
        else if (command instanceof CloseConnection)
        {
            final CloseConnection cmd = (CloseConnection)command;
            socketApi.closeConnection(cmd.port(), cmd.connectionId());
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void work()
    {
        socketApi.work();
    }

    @Override
    public void close()
    {
        socketApi.close();
    }
}
