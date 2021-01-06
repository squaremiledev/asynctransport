package dev.squaremile.asynctcp.internal.serialization;

import java.util.function.Function;
import java.util.stream.Stream;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import dev.squaremile.asynctcp.api.transport.app.Transport;
import dev.squaremile.asynctcp.api.transport.app.TransportCommand;
import dev.squaremile.asynctcp.api.transport.app.TransportUserCommand;
import dev.squaremile.asynctcp.api.transport.commands.CloseConnection;
import dev.squaremile.asynctcp.api.transport.events.Connected;
import dev.squaremile.asynctcp.api.transport.values.ConnectionId;
import dev.squaremile.asynctcp.api.transport.values.ConnectionIdValue;
import dev.squaremile.asynctcp.fixtures.transport.CommandsProvidingTransport;
import dev.squaremile.asynctcp.fixtures.transport.TransportCommandSpy;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.rawStreaming;
import static dev.squaremile.asynctcp.Assertions.assertEqual;

class SerializingTransportTest
{
    private static final int OFFSET = 6;
    private static final Connected CONNECTED_EVENT = Fixtures.connectedEvent();

    private final TransportCommandSpy commandsSpy = new TransportCommandSpy(new CommandsProvidingTransport(1024, rawStreaming()));
    private final TransportCommandDecoders decoders = new TransportCommandDecoders(new CommandsProvidingTransport(CONNECTED_EVENT.outboundPduLimit(), rawStreaming()));

    static Stream<Function<Transport, TransportUserCommand>> commands()
    {
        return Fixtures.commands();
    }

    @ParameterizedTest
    @MethodSource("commands")
    void shouldSerializeCommands(final Function<Transport, TransportCommand> commandProvider)
    {
        // Given
        SerializingTransport transport = new SerializingTransport(
                new UnsafeBuffer(new byte[100]),
                OFFSET,
                (buffer, offset, length) -> commandsSpy.handle(decoders.decode(buffer, offset, length))
        );
        transport.onEvent(CONNECTED_EVENT);
        TransportCommand command = commandProvider.apply(transport);

        // When
        transport.handle(command);

        // Then
        assertEqual(commandsSpy.all(), command.copy());
    }

    @Test
    void shouldProvideConnectionCommandsForExistingConnections()
    {
        // Given
        SerializingTransport transport = new SerializingTransport(
                new UnsafeBuffer(new byte[100]),
                OFFSET,
                (buffer, offset, length) -> commandsSpy.handle(decoders.decode(buffer, offset, length))
        );
        transport.onEvent(CONNECTED_EVENT);

        // When
        CloseConnection command = transport.command(CONNECTED_EVENT.connectionId(), CloseConnection.class).set(123);

        // Then
        assertThat(command.commandId()).isEqualTo(123);
        assertThat(command.connectionId()).isEqualTo(CONNECTED_EVENT.connectionId());
        assertThat(command.port()).isEqualTo(CONNECTED_EVENT.port());
        assertThrows(IllegalArgumentException.class, () -> transport.command(
                ((ConnectionId)new ConnectionIdValue(CONNECTED_EVENT.port(), CONNECTED_EVENT.connectionId() + 10)).connectionId(),
                CloseConnection.class
        ));
    }

    @Test
    void shouldProvideConnectionCommandsForNonExistingConnections()
    {
        // Given
        SerializingTransport transport = new SerializingTransport(
                new UnsafeBuffer(new byte[100]),
                OFFSET,
                (buffer, offset, length) -> commandsSpy.handle(decoders.decode(buffer, offset, length))
        );

        // Then
        assertThrows(
                IllegalArgumentException.class,
                () -> transport.command(((ConnectionId)new ConnectionIdValue(4444, 9765)).connectionId(), CloseConnection.class)
        );
    }
}