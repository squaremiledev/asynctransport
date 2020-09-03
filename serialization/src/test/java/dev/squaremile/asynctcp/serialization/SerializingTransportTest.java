package dev.squaremile.asynctcp.serialization;

import java.util.function.Function;
import java.util.stream.Stream;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


import dev.squaremile.asynctcp.domain.api.ConnectionIdValue;
import dev.squaremile.asynctcp.domain.api.Transport;
import dev.squaremile.asynctcp.domain.api.commands.CloseConnection;
import dev.squaremile.asynctcp.domain.api.commands.TransportCommand;
import dev.squaremile.asynctcp.sbe.MessageHeaderDecoder;
import dev.squaremile.asynctcp.testfixtures.TransportCommandSpy;

import static dev.squaremile.asynctcp.testfixtures.Assertions.assertEqual;

class SerializingTransportTest
{
    private static final int OFFSET = 6;
    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private final TransportCommandSpy commandsSpy = new TransportCommandSpy();
    private final TransportCommandDecoders decoders = new TransportCommandDecoders();

    static Stream<Function<Transport, TransportCommand>> commands()
    {
        return Stream.of(
                // TODO: retrieve from the provided transport
                transport -> new CloseConnection(new ConnectionIdValue(8881, 3)).set(201)
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
        TransportCommand command = commandProvider.apply(transport);

        // When
        transport.handle(command);

        // Then
        assertEqual(commandsSpy.all(), command);
    }
}