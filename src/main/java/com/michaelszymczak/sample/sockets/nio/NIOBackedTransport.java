package com.michaelszymczak.sample.sockets.nio;

import java.io.IOException;

import com.michaelszymczak.sample.sockets.api.Transport;
import com.michaelszymczak.sample.sockets.api.commands.TransportCommand;
import com.michaelszymczak.sample.sockets.api.TransportEventsListener;
import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.commands.StopListening;

public class NIOBackedTransport implements AutoCloseable, Transport
{
    private final TransportEventsListener transportEventsListener;
    private SocketApi socketApi;

    public NIOBackedTransport(final TransportEventsListener transportEventsListener) throws IOException
    {
        this.socketApi = new SocketApi(transportEventsListener);
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
        if (command instanceof StopListening)
        {
            final StopListening cmd = (StopListening)command;
            transportEventsListener.onEvent(socketApi.stopListening(cmd.port(), cmd.commandId()));
        }
    }

    public void doWork()
    {
        socketApi.doWork();
    }

    @Override
    public void close()
    {
        socketApi.close();
    }
}
