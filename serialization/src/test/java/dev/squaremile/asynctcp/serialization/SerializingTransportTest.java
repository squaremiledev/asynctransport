package dev.squaremile.asynctcp.serialization;

import java.util.function.Function;
import java.util.stream.Stream;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import dev.squaremile.asynctcp.api.app.Transport;
import dev.squaremile.asynctcp.api.app.TransportCommand;
import dev.squaremile.asynctcp.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.api.commands.CloseConnection;
import dev.squaremile.asynctcp.api.events.Connected;
import dev.squaremile.asynctcp.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.testfixtures.TransportCommandSpy;

import static dev.squaremile.asynctcp.testfixtures.Assertions.assertEqual;

class SerializingTransportTest
{
    private static final int OFFSET = 6;
    private static final Connected CONNECTED_EVENT = Fixtures.connectedEvent();

    private final TransportCommandSpy commandsSpy = new TransportCommandSpy();
    private final TransportCommandDecoders decoders = new TransportCommandDecoders();

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
                (buffer, offset) -> commandsSpy.handle(decoders.decode(buffer, offset))
        );
        transport.onEvent(CONNECTED_EVENT);
        TransportCommand command = commandProvider.apply(transport);

        // When
        transport.handle(command);

        // Then
        assertEqual(commandsSpy.all(), command);
    }

    @Test
    void shouldProvideConnectionCommandsForExistingConnections()
    {
        // Given
        SerializingTransport transport = new SerializingTransport(
                new UnsafeBuffer(new byte[100]),
                OFFSET,
                (buffer, offset) -> commandsSpy.handle(decoders.decode(buffer, offset))
        );
        transport.onEvent(CONNECTED_EVENT);

        // When
        CloseConnection command = transport.command(CONNECTED_EVENT, CloseConnection.class).set(123);

        // Then
        assertThat(command.commandId()).isEqualTo(123);
        assertThat(command.connectionId()).isEqualTo(CONNECTED_EVENT.connectionId());
        assertThat(command.port()).isEqualTo(CONNECTED_EVENT.port());
        assertThrows(IllegalArgumentException.class, () ->
                transport.command(new ConnectionIdValue(CONNECTED_EVENT.port(), CONNECTED_EVENT.connectionId() + 10), CloseConnection.class));
    }

    @Test
    void shouldProvideConnectionCommandsForNonExistingConnections()
    {
        // Given
        SerializingTransport transport = new SerializingTransport(
                new UnsafeBuffer(new byte[100]),
                OFFSET,
                (buffer, offset) -> commandsSpy.handle(decoders.decode(buffer, offset))
        );

        // Then
        assertThrows(
                IllegalArgumentException.class,
                () -> transport.command(new ConnectionIdValue(4444, 9765), CloseConnection.class)
        );
    }
}