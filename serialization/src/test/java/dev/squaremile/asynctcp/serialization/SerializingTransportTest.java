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
import dev.squaremile.asynctcp.api.commands.CloseConnection;
import dev.squaremile.asynctcp.api.commands.Connect;
import dev.squaremile.asynctcp.api.commands.Listen;
import dev.squaremile.asynctcp.api.commands.SendData;
import dev.squaremile.asynctcp.api.commands.StopListening;
import dev.squaremile.asynctcp.api.app.TransportCommand;
import dev.squaremile.asynctcp.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.api.events.Connected;
import dev.squaremile.asynctcp.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.api.values.PredefinedTransportEncoding;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.testfixtures.TransportCommandSpy;

import static dev.squaremile.asynctcp.testfixtures.Assertions.assertEqual;

class SerializingTransportTest
{
    private static final int OFFSET = 6;
    private static final Connected CONNECTED_EVENT = new Connected(8881, 3, "remoteHost", 8882, 4, 56000, 80000);

    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private final TransportCommandSpy commandsSpy = new TransportCommandSpy();
    private final TransportCommandDecoders decoders = new TransportCommandDecoders();

    static Stream<Function<Transport, TransportUserCommand>> commands()
    {
        return Stream.of(
                transport -> transport.command(CONNECTED_EVENT, CloseConnection.class).set(201),
                transport -> transport.command(Connect.class).set("remoteHost", 8899, 202, 10, PredefinedTransportEncoding.SINGLE_BYTE),
                transport -> transport.command(Listen.class).set(203, 6688, PredefinedTransportEncoding.LONGS),
                transport -> transport.command(CONNECTED_EVENT, SendData.class).set(new byte[]{1, 2, 3, 4, 5, 6}, 205),
                transport -> transport.command(StopListening.class).set(204, 7788)
        );
    }

    @ParameterizedTest
    @MethodSource("commands")
    void shouldSerializeCommands(final Function<Transport, TransportCommand> commandProvider)
    {
        // Given
        SerializingTransport transport = new SerializingTransport(
                new UnsafeBuffer(new byte[100]),
                OFFSET,
                (buffer, offset) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    commandsSpy.handle(decoders.commandDecoderForTemplateId(headerDecoder.templateId()).decode(buffer, offset));
                }
        );
        transport.onEvent(CONNECTED_EVENT);
        TransportCommand command = commandProvider.apply(transport);

        // When
        transport.handle(command);

        // Then
        assertEqual(commandsSpy.all(), command);
    }

    @Test
    void shouldNotProvideConnectionCommandsForExistingConnectionsOnly()
    {
        // Given
        SerializingTransport transport = new SerializingTransport(
                new UnsafeBuffer(new byte[100]),
                OFFSET,
                (buffer, offset) ->
                {
                    headerDecoder.wrap(buffer, offset);
                    commandsSpy.handle(decoders.commandDecoderForTemplateId(headerDecoder.templateId()).decode(buffer, offset));
                }
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
}