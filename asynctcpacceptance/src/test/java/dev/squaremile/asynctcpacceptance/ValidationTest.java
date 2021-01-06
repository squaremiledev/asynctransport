package dev.squaremile.asynctcpacceptance;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.agrona.collections.MutableReference;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation;
import dev.squaremile.asynctcp.api.transport.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.CommandFailed;
import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.Transport;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportCommand;
import dev.squaremile.asynctcp.api.transport.app.TransportUserCommand;
import dev.squaremile.asynctcp.api.transport.commands.Connect;
import dev.squaremile.asynctcp.api.transport.commands.Listen;
import dev.squaremile.asynctcp.api.transport.events.StartedListening;
import dev.squaremile.asynctcp.api.transport.events.TransportCommandFailed;
import dev.squaremile.asynctcp.api.transport.values.Delineation;
import dev.squaremile.asynctcp.fixtures.transport.EventsSpy;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.fixMessage;
import static dev.squaremile.asynctcpacceptance.Assertions.assertEqual;
import static dev.squaremile.asynctcp.fixtures.transport.FreePort.freePort;

@ExtendWith(TimingExtension.class)
public class ValidationTest
{
    private final EventsSpy events = EventsSpy.spy();

    static Stream<Delineation> unsupportedDelineation()
    {
        return Stream.of(
                new Delineation(Delineation.Type.ASCII_PATTERN, 0, 1, "invalidPattern"),
                new Delineation(Delineation.Type.FIXED_LENGTH, 0, -1, ""),
                new Delineation(Delineation.Type.FIXED_LENGTH, -1, 8, ""),
                new Delineation(Delineation.Type.FIXED_LENGTH, 0, 8, fixMessage().pattern()),
                new Delineation(
                        Delineation.Type.ASCII_PATTERN,
                        0,
                        fixMessage().extraLength() + 1,
                        fixMessage().pattern()
                ),
                new Delineation(Delineation.Type.ASCII_PATTERN, 0, fixMessage().extraLength(), fixMessage().pattern() + "a"
                )
        );
    }

    static Stream<Delineation> supportedDelineation()
    {
        return Stream.of(
                new Delineation(Delineation.Type.FIXED_LENGTH, 0, 8, ""),
                PredefinedTransportDelineation.fixMessage(),
                PredefinedTransportDelineation.rawStreaming(),
                PredefinedTransportDelineation.fixedLengthDelineation(5),
                PredefinedTransportDelineation.lengthBasedDelineation(Delineation.Type.INT_BIG_ENDIAN_FIELD, 3, 4),
                PredefinedTransportDelineation.lengthBasedDelineation(Delineation.Type.SHORT_LITTLE_ENDIAN_FIELD, 0, 0)
        );
    }

    static Stream<Function<Transport, TransportUserCommand>> commandsWithUnsupportedDelineation()
    {
        Delineation unsupportedDelineation = new Delineation(Delineation.Type.ASCII_PATTERN, 0, 1, "invalidPattern");
        return Stream.of(
                transport -> transport.command(Listen.class).set(1, 8888, unsupportedDelineation),
                transport -> transport.command(Connect.class).set("host", 9999, 1, 20, unsupportedDelineation)
        );
    }

    @ParameterizedTest
    @MethodSource("unsupportedDelineation")
    void shouldRejectUnsupportedDelineation(final Delineation delineation)
    {
        // When
        whenApplicationStarting(events, transport -> transport.handle(
                transport.command(Listen.class).set(1, 8888, delineation)));

        // Then
        assertThat(events.contains(CommandFailed.class)).isTrue();
        assertThat(events.contains(StartedListening.class)).isFalse();
        CommandFailed actualEvent = events.last(CommandFailed.class);
        assertEqual(events.all(), new TransportCommandFailed(8888, 1, actualEvent.details(), Listen.class));
        assertThat(actualEvent.details()).containsIgnoringCase("delineation");
    }

    @ParameterizedTest
    @MethodSource("supportedDelineation")
    void shouldHandleSupportedDelineation(final Delineation delineation)
    {
        // When
        whenApplicationStarting(events, transport -> transport.handle(
                transport.command(Listen.class).set(1, freePort(), delineation)));

        // Then
        assertThat(events.contains(StartedListening.class)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("commandsWithUnsupportedDelineation")
    void shouldNotAllowListeningWhenProvidedDelineationIsNotSupported(final Function<Transport, TransportUserCommand> command)
    {
        // When
        final MutableReference<TransportCommand> handledCommand = new MutableReference<>();
        whenApplicationStarting(events, transport ->
        {
            TransportUserCommand cmd = command.apply(transport);
            handledCommand.set(cmd);
            transport.handle(cmd);
        });

        // Then
        CommandFailed actualEvent = events.last(CommandFailed.class);
        assertEqual(events.all(), new TransportCommandFailed(handledCommand.get().port(), handledCommand.get().commandId(), actualEvent.details(), handledCommand.get().getClass()));
        assertThat(actualEvent.details()).containsIgnoringCase("delineation");
    }

    private void whenApplicationStarting(final EventsSpy eventsSpy, final Consumer<Transport> onStart)
    {
        ApplicationOnDuty application = new AsyncTcp().createSharedStack("test", transport -> new TransportApplicationOnDuty()
        {
            @Override
            public void onStart()
            {
                onStart.accept(transport);
            }

            @Override
            public void onEvent(final Event event)
            {
                eventsSpy.onEvent(event);
            }
        });
        application.onStart();
        application.work();
    }
}
