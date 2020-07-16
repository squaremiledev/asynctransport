package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import com.michaelszymczak.sample.sockets.api.ConnectionId;
import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.commands.SendData;
import com.michaelszymczak.sample.sockets.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.api.events.DataSent;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;

import static org.assertj.core.api.Assertions.assertThat;


import static com.michaelszymczak.sample.sockets.support.BackgroundRunner.completed;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePortOtherThan;
import static java.nio.charset.StandardCharsets.US_ASCII;

public class TransportDriver
{

    private final TransportUnderTest transport;
    private int nextCommandId = 0;

    public TransportDriver(final TransportUnderTest transport)
    {
        this.transport = transport;
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
        transport.workUntil(completed(() -> client.connectedTo(startedListeningEvent.port(), clientPort)));
        final Predicate<ConnectionAccepted> connectionAcceptedPredicate = event -> event.commandId() == startedListeningEvent.commandId() && event.remotePort() == clientPort;
        transport.workUntil(() -> !transport.events().all(ConnectionAccepted.class, connectionAcceptedPredicate).isEmpty());
        return transport.events().last(ConnectionAccepted.class, connectionAcceptedPredicate);
    }

    public StartedListening startListening()
    {
        return startListening(freePort());
    }

    public StartedListening startListening(final int port)
    {
        final int commandId = nextCommandId++;
        transport.handle(new Listen(commandId, port));
        return transport.events().last(StartedListening.class, event -> event.commandId() == commandId);
    }

    public void successfullySendToClient(final ConnectionId connection, final SampleClient client, final String message)
    {
        final byte[] content = message.getBytes(US_ASCII);
        final long connectionId = connection.connectionId();
        final long totalBytesSentBefore = (transport.events().contains(DataSent.class, event -> event.connectionId() == connectionId)) ?
                                          transport.events().last(DataSent.class, event -> event.connectionId() == connectionId).totalBytesSent() :
                                          0;
        final long randomCommandId = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
        transport.handle(new SendData(connection.port(), connection.connectionId(), content, randomCommandId));
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        transport.workUntil(completed(() -> client.read(content.length, content.length, dataConsumer)));
        transport.workUntil(() -> transport.events().contains(DataSent.class, event -> event.commandId() == randomCommandId));
        transport.workUntil(() -> transport.events().last(DataSent.class, event -> event.commandId() == randomCommandId)
                                          .totalBytesSent() >= totalBytesSentBefore + content.length);
        assertThat(dataConsumer.dataRead()).isEqualTo(content);
    }

}
