package com.michaelszymczak.sample.sockets;

import com.michaelszymczak.sample.sockets.commands.Command;
import com.michaelszymczak.sample.sockets.commands.Listen;
import com.michaelszymczak.sample.sockets.commands.StopListening;
import com.michaelszymczak.sample.sockets.events.EventsListener;

public class ReactiveConnections implements AutoCloseable
{
    private final EventsListener eventsListener;
    private SocketApi socketApi;

    public ReactiveConnections(final EventsListener eventsListener)
    {
        this.socketApi = new SocketApi();
        this.eventsListener = eventsListener;
    }

    public void handle(final Command command)
    {
        if (command instanceof Listen)
        {
            final Listen cmd = (Listen)command;
            eventsListener.onEvent(socketApi.listen(cmd.commandId(), cmd.port()));
        }
        if (command instanceof StopListening)
        {
            final StopListening cmd = (StopListening)command;
            eventsListener.onEvent(socketApi.stopListening(cmd.commandId(), cmd.sessionId()));
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
