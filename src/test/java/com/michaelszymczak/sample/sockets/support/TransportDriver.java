package com.michaelszymczak.sample.sockets.support;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import com.michaelszymczak.sample.sockets.domain.api.CommandId;
import com.michaelszymczak.sample.sockets.domain.api.ConnectionId;
import com.michaelszymczak.sample.sockets.domain.api.commands.Connect;
import com.michaelszymczak.sample.sockets.domain.api.commands.Listen;
import com.michaelszymczak.sample.sockets.domain.api.commands.SendData;
import com.michaelszymczak.sample.sockets.domain.api.events.Connected;
import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionAccepted;
import com.michaelszymczak.sample.sockets.domain.api.events.DataSent;
import com.michaelszymczak.sample.sockets.domain.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.domain.api.events.TransportEvent;

import org.agrona.collections.MutableInteger;

import static org.assertj.core.api.Assertions.assertThat;


import static com.michaelszymczak.sample.sockets.support.BackgroundRunner.completed;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePortOtherThan;
import static com.michaelszymczak.sample.sockets.support.StringFixtures.byteArrayWith;
import static java.nio.charset.StandardCharsets.US_ASCII;

public class TransportDriver
{

    private final TransportUnderTest transport;
    private int nextCommandId = 0;

    public TransportDriver(final TransportUnderTest transport)
    {
        this.transport = transport;
    }

    public DataSent fillTheSendingWindow(final ConnectionId connectionId, final int maxMessageSize)
    {
        final int totalNumberOfEventsBefore = transport.events().all(TransportEvent.class).size();
        final byte[] singleMessageData = byteArrayWith(pos -> String.format("%9d%n", pos), maxMessageSize / 10);
        assertThat(singleMessageData.length).isEqualTo(maxMessageSize);

        //When
        MutableInteger commandsCount = new MutableInteger(0);
        transport.workUntil(() ->
                            {
                                transport.handle(transport.command(connectionId, SendData.class).set(singleMessageData, commandsCount.incrementAndGet()));
                                // stop when unable to send more data
                                return !transport.connectionEvents().all(DataSent.class, connectionId.connectionId()).isEmpty() &&
                                       transport.connectionEvents().last(DataSent.class, connectionId.connectionId()).bytesSent() == 0;
                            });
        final int commandsSentCount = commandsCount.get();

        // Then
        assertThat(transport.events().all(TransportEvent.class)).hasSize(totalNumberOfEventsBefore + commandsSentCount);
        final DataSent lastEvent = transport.connectionEvents().last(DataSent.class, connectionId.connectionId());
        assertThat(lastEvent.bytesSent()).isEqualTo(0);
        assertThat(lastEvent.commandId()).isEqualTo(commandsSentCount);
        final int dataSizeInAllCommands = singleMessageData.length * commandsSentCount;
        assertThat(lastEvent.totalBytesBuffered()).isEqualTo(dataSizeInAllCommands);
        final int totalDataSentByIndividualChunks = (int)transport.connectionEvents().all(DataSent.class, connectionId.connectionId()).stream().mapToLong(DataSent::bytesSent).sum();
        assertThat(lastEvent.totalBytesSent()).isEqualTo(totalDataSentByIndividualChunks);
        return transport.connectionEvents().last(DataSent.class, connectionId.connectionId());
    }

    public ConnectionAccepted listenAndConnect(final SampleClient client)
    {
        final int serverPort = freePort();
        return listenAndConnect(client, serverPort, freePortOtherThan(serverPort));
    }

    public ConnectionAccepted listenAndConnect(final TransportUnderTest client)
    {
        final int serverPort = freePort();
        final StartedListening startedListeningEvent = startListening(serverPort);
        int acceptedEventsBefore = transport.connectionEvents().all(ConnectionAccepted.class).size();
        int connectedEventsBefore = client.connectionEvents().all(Connected.class).size();
        client.handle(new Connect().set(startedListeningEvent.port(), CommandId.NO_COMMAND_ID));
        Worker.runUntil(() ->
                        {
                            transport.work();
                            client.work();
                            return client.connectionEvents().all(Connected.class).size() > connectedEventsBefore &&
                                   transport.connectionEvents().all(ConnectionAccepted.class).size() > acceptedEventsBefore;
                        });
        return transport.events().last(ConnectionAccepted.class);
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
        transport.handle(transport.command(Listen.class).set(commandId, port));
        return transport.events().last(StartedListening.class, event -> event.commandId() == commandId);
    }

    public void successfullySendToClient(final ConnectionId connection, final SampleClient client, final String message)
    {
        final byte[] content = message.getBytes(US_ASCII);
        final long connectionId = connection.connectionId();
        final long totalBytesSentBefore = (transport.connectionEvents().contains(DataSent.class, connectionId)) ?
                                          transport.connectionEvents().last(DataSent.class, connectionId).totalBytesSent() :
                                          0;
        final long randomCommandId = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
        connection.port();
        connection.connectionId();
        transport.handle(transport.command(connection, SendData.class).set(content, randomCommandId));
        final ThreadSafeReadDataSpy dataConsumer = new ThreadSafeReadDataSpy();
        transport.workUntil(completed(() -> client.read(content.length, content.length, dataConsumer)));
        transport.workUntil(() -> transport.events().contains(DataSent.class, event -> event.commandId() == randomCommandId));
        transport.workUntil(() -> transport.events().last(DataSent.class, event -> event.commandId() == randomCommandId)
                                          .totalBytesSent() >= totalBytesSentBefore + content.length);
        assertThat(dataConsumer.dataRead()).isEqualTo(content);
    }

}
