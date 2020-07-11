package com.michaelszymczak.sample.sockets.support;

import com.michaelszymczak.sample.sockets.api.Transport;
import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;


import static com.michaelszymczak.sample.sockets.support.Foreman.workUntil;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;

public class TransportDriver
{

    private final Transport transport;
    private final TransportEvents events;
    private final BackgroundRunner runner = new BackgroundRunner();
    private int nextCommandId = 0;

    public TransportDriver(final Transport transport, final TransportEvents events)
    {
        this.transport = transport;
        this.events = events;
    }

    public ConnectionAccepted listenAndConnect(final SampleClient client)
    {
        return listenAndConnect(client, nextCommandId++, freePort());
    }

    public ConnectionAccepted listenAndConnect(final SampleClient client, final int commandId, final int port)
    {
        transport.handle(new Listen(commandId, port));
        final int serverPort = events.last(StartedListening.class).port();
        runner.keepRunning(transport::work).untilCompleted(() -> client.connectedTo(serverPort));
        workUntil(() -> !events.all(ConnectionAccepted.class, event -> event.commandId() == commandId).isEmpty(), transport);
        return events.last(ConnectionAccepted.class);
    }
}
