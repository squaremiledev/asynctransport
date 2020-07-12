package com.michaelszymczak.sample.sockets.support;

import java.util.function.Predicate;

import com.michaelszymczak.sample.sockets.api.Transport;
import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;


import static com.michaelszymczak.sample.sockets.support.Foreman.workUntil;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePortOtherThan;

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
        final int serverPort = freePort();
        return listenAndConnect(client, serverPort, freePortOtherThan(serverPort));
    }

    public ConnectionAccepted listenAndConnect(final SampleClient client, final int serverPort, final int clientPort)
    {
        final StartedListening startedListeningEvent = startListening(serverPort);
        return connectClient(startedListeningEvent, client, clientPort);
    }

    public ConnectionAccepted connectClient(StartedListening startedListeningEvent, final SampleClient client)
    {
        return connectClient(startedListeningEvent, client, freePort());
    }

    public ConnectionAccepted connectClient(StartedListening startedListeningEvent, final SampleClient client, final int clientPort)
    {
        runner.keepRunning(transport::work).untilCompleted(() -> client.connectedTo(startedListeningEvent.port(), clientPort));
        final Predicate<ConnectionAccepted> connectionAcceptedPredicate = event -> event.commandId() == startedListeningEvent.commandId() && event.remotePort() == clientPort;
        workUntil(() -> !events.all(ConnectionAccepted.class, connectionAcceptedPredicate).isEmpty(), transport);
        return events.last(ConnectionAccepted.class, connectionAcceptedPredicate);
    }

    public StartedListening startListening()
    {
        return startListening(freePort());
    }

    public StartedListening startListening(final int port)
    {
        final int commandId = nextCommandId++;
        transport.handle(new Listen(commandId, port));
        return events.last(StartedListening.class, event -> event.commandId() == commandId);
    }

}
