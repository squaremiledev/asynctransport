package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;

import com.michaelszymczak.sample.sockets.api.commands.Listen;
import com.michaelszymczak.sample.sockets.api.commands.StopListening;
import com.michaelszymczak.sample.sockets.api.events.StartedListening;
import com.michaelszymczak.sample.sockets.api.events.StoppedListening;
import com.michaelszymczak.sample.sockets.api.events.TransportCommandFailed;
import com.michaelszymczak.sample.sockets.support.SampleClient;
import com.michaelszymczak.sample.sockets.support.SampleClients;
import com.michaelszymczak.sample.sockets.support.TransportUnderTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static com.michaelszymczak.sample.sockets.support.Assertions.assertEqual;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePort;
import static com.michaelszymczak.sample.sockets.support.FreePort.freePortOtherThan;
import static com.michaelszymczak.sample.sockets.support.TearDown.closeCleanly;

class ListeningTransportTest
{

    private final TransportUnderTest transport;
    private final SampleClients clients;

    ListeningTransportTest() throws SocketException
    {
        transport = new TransportUnderTest();
        clients = new SampleClients();
    }

    @Test
    void shouldAcceptConnections() throws IOException
    {
        final int port = freePort();

        // When
        transport.handle(transport.command(Listen.class).set(7, port));
        transport.workUntil(() -> transport.events().contains(StartedListening.class));

        // Then
        transport.events().last(StartedListening.class);
        assertEqual(transport.events().all(StartedListening.class), new StartedListening(port, 7));
        clients.client(1).connectedTo(port);
    }

    @Test
    void shouldNotAcceptIfNotAsked()
    {
        assertThrows(ConnectException.class, () -> clients.client(1).connectedTo(freePort()));
    }

    @Test
    void shouldStopListeningWhenAsked()
    {
        transport.handle(transport.command(Listen.class).set(0, freePort()));
        transport.workUntil(() -> transport.events().contains(StartedListening.class));
        final int port = transport.events().last(StartedListening.class).port();

        // When
        transport.handle(transport.command(StopListening.class).set(9, port));
        transport.workUntil(() -> transport.events().contains(StoppedListening.class));

        // Then
        assertEqual(transport.events().all(StoppedListening.class), new StoppedListening(port, 9));
        assertThrows(ConnectException.class, () -> clients.client(1).connectedTo(port));
    }

    @Test
    void shouldIgnoreStopListeningCommandForNonExistingRequest() throws IOException
    {
        // Given
        final int port = freePort();
        final int anotherPort = freePortOtherThan(port);
        transport.handle(transport.command(Listen.class).set(2, port));
        transport.workUntil(() -> transport.events().contains(StartedListening.class));

        // When
        transport.handle(transport.command(StopListening.class).set(4, anotherPort));
        transport.workUntil(() -> transport.events().contains(TransportCommandFailed.class));

        // Then
        final TransportCommandFailed event = transport.events().last(TransportCommandFailed.class);
        assertThat(event.commandId()).isEqualTo(4);
        assertThat(event.port()).isEqualTo(anotherPort);
        clients.client(1).connectedTo(port);
    }

    @Test
    void shouldBeAbleToListenOnMoreThanOnePort() throws Exception
    {
        // When
        final int port1 = freePort();
        transport.handle(transport.command(Listen.class).set(0, port1));
        final int port2 = freePortOtherThan(port1);
        transport.handle(transport.command(Listen.class).set(1, port2));
        transport.workUntil(() -> transport.events().all(StartedListening.class).size() == 2);

        // Then
        clients.client(1).connectedTo(port1);
        clients.client(2).connectedTo(port2);
    }


    @Test
    void shouldUseRequestIdToFindThePortItShouldStopListeningOn() throws Exception
    {
        // Given
        final int port1 = freePort();
        final int port2 = freePortOtherThan(port1);
        final int port3 = freePortOtherThan(port1, port2);
        assertThrows(ConnectException.class, () -> new SampleClient().connectedTo(port1));
        assertThrows(ConnectException.class, () -> new SampleClient().connectedTo(port2));
        assertThrows(ConnectException.class, () -> new SampleClient().connectedTo(port3));

        transport.handle(transport.command(Listen.class).set(5, port1));
        transport.handle(transport.command(Listen.class).set(6, port2));
        transport.handle(transport.command(Listen.class).set(7, port3));
        transport.workUntil(() -> transport.events().all(StartedListening.class).size() == 3);
        assertEqual(transport.events().all(StartedListening.class), new StartedListening(port1, 5), new StartedListening(port2, 6), new StartedListening(port3, 7));

        // When
        transport.handle(transport.command(StopListening.class).set(9, port2));
        transport.workUntil(() -> transport.events().contains(StoppedListening.class));

        // Then
        assertThat(transport.events().last(StoppedListening.class).commandId()).isEqualTo(9);
        assertThat(transport.events().last(StoppedListening.class).port()).isEqualTo(port2);
        clients.client(1).connectedTo(port1);
        assertThrows(ConnectException.class, () -> clients.client(2).connectedTo(port2));
        clients.client(3).connectedTo(port3);
    }

    @AfterEach
    void tearDown()
    {
        closeCleanly(transport, clients, transport.statusEvents());
    }
}
