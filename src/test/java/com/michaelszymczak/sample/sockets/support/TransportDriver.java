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

    public TransportDriver(final Transport transport, final TransportEvents events)
    {
        this.transport = transport;
        this.events = events;
    }

    public ConnectionAccepted connect(final SampleClient client)
    {
        transport.handle(new Listen(1, freePort()));
        final int serverPort = events.last(StartedListening.class).port();
        runner.keepRunning(transport::work).untilCompleted(() -> client.connectedTo(serverPort));
        workUntil(() -> !events.all(ConnectionAccepted.class).isEmpty(), transport);
        return events.last(ConnectionAccepted.class);
    }
}
