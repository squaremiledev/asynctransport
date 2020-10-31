package dev.squaremile.asynctcp.serialization.internal;

import java.util.function.Function;
import java.util.stream.Stream;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.testfixtures.CommandsProvidingTransport;
import dev.squaremile.asynctcp.transport.testfixtures.TransportCommandSpy;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.rawStreaming;
import static dev.squaremile.asynctcp.transport.testfixtures.Assertions.assertEqual;

class TransportCommandDeserializationTest
{
    private static final int OFFSET = 6;
    private final TransportCommandSpy commandsSpy = new TransportCommandSpy(new CommandsProvidingTransport(Fixtures.connectedEvent().outboundPduLimit(), rawStreaming()));

    static Stream<Function<Transport, TransportUserCommand>> commands()
    {
        return Fixtures.commands();
    }

    @ParameterizedTest
    @MethodSource("commands")
    void shouldSerializeCommands(final Function<Transport, TransportCommand> commandProvider)
    {
        // Given
        SerializingTransport transport = notifyingAboutSerializedCommand(new TransportCommandDeserialization(commandsSpy));
        TransportCommand command = commandProvider.apply(transport);

        // When
        transport.handle(command);

        // Then
        assertEqual(commandsSpy.all(), command.copy());
    }

    private SerializingTransport notifyingAboutSerializedCommand(final TransportCommandDeserialization serializedCommandListener)
    {
        SerializingTransport transport = new SerializingTransport(
                new UnsafeBuffer(new byte[150]),
                OFFSET,
                serializedCommandListener
        );
        transport.onEvent(Fixtures.connectedEvent());
        return transport;
    }
}