package com.michaelszymczak.sample.sockets;

import com.michaelszymczak.sample.sockets.commands.Listen;
import com.michaelszymczak.sample.sockets.commands.StopListening;
import com.michaelszymczak.sample.sockets.commands.TransportCommand;
import com.michaelszymczak.sample.sockets.events.TransportEventsListener;

public class Transport implements AutoCloseable
{
    private final TransportEventsListener transportEventsListener;
    private SocketApi socketApi;

    public Transport(final TransportEventsListener transportEventsListener)
    {
        this.socketApi = new SocketApi();
        this.transportEventsListener = transportEventsListener;
    }

    public void handle(final TransportCommand command)
    {
        if (command instanceof Listen)
        {
            final Listen cmd = (Listen)command;
            transportEventsListener.onEvent(socketApi.listen(cmd.commandId(), cmd.port()));
        }
        if (command instanceof StopListening)
        {
            final StopListening cmd = (StopListening)command;
            transportEventsListener.onEvent(socketApi.stopListening(cmd.commandId(), cmd.port()));
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
