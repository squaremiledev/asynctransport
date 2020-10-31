package dev.squaremile.asynctcp.serialization.internal;

import java.util.function.Function;
import java.util.stream.Stream;


import dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.commands.StopListening;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionCommandFailed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.transport.api.events.DataSent;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.events.StoppedListening;
import dev.squaremile.asynctcp.transport.api.events.TransportCommandFailed;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.fixMessage;
import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.rawStreaming;

class Fixtures
{
    /**
     * @return All events that can be processed by a separate component
     * <p>
     * Delineation process applied to DataReceived will result in zero or more MessageReceived events, thus
     * DataReceived event itself is not currently serialized.
     * <p>
     * The MessageReceived type is serializable, but has its own test as the offset in the buffer is not guaranteed
     * to remain the same due to compacting to avoid copying large amount of unnecessary data
     */
    static Stream<TransportEvent> oneToOneSerializableEvents()
    {
        return Stream.of(
                new Connected(8881, 3, "remoteHost", 8882, 4, 56000, 80000, fixMessage()),
                new Connected(8882, 4, "remoteHost2", 8882, 4, 16000, 20000, PredefinedTransportDelineation.fixedLengthDelineation(8)),
                new Connected(8882, 4, "remoteHost2", 8882, 4, 16000, 20000, new Delineation(Delineation.Type.INT_BIG_ENDIAN_FIELD, 30, 50, "")),
                new ConnectionAccepted(9881, 4, "remote", 9882, 5, 46000, 30000, fixMessage()),
                new ConnectionAccepted(9881, 4, "remote", 9882, 5, 46000, 30000, new Delineation(Delineation.Type.INT_BIG_ENDIAN_FIELD, 30, 50, "")),
                new ConnectionClosed(7888, 1, 2),
                new ConnectionCommandFailed(8884, 103, "some details", 6),
                new ConnectionResetByPeer(5888, 4, 6),
                new DataSent(5888, 4, 1, 9, 18, 104, 45_000),
                new StartedListening(8888, 5, fixMessage()),
                new StartedListening(8881, 4, PredefinedTransportDelineation.fixedLengthDelineation(8)),
                new StartedListening(8881, 4, new Delineation(Delineation.Type.INT_BIG_ENDIAN_FIELD, 20, 40, "")),
                new StoppedListening(8988, 6),
                new TransportCommandFailed(8001, 101L, "some details", Listen.class)

        );
    }

    static Stream<Function<Transport, TransportUserCommand>> commands()
    {
        return Stream.of(
                transport -> transport.command(connectedEvent(), CloseConnection.class).set(201),
                transport -> transport.command(Connect.class).set("remoteHost", 8899, 202, 10, new Delineation(Delineation.Type.FIXED_LENGTH, 0, 1, "")),
                transport -> transport.command(Connect.class).set("remoteHost2", 8898, 203, 11, fixMessage()),
                transport -> transport.command(Connect.class).set("remoteHost2", 8898, 203, 11, new Delineation(Delineation.Type.INT_BIG_ENDIAN_FIELD, 10, 20, "")),
                transport -> transport.command(Listen.class).set(203, 6688, PredefinedTransportDelineation.fixedLengthDelineation(8)),
                transport -> transport.command(Listen.class).set(204, 6689, new Delineation(Delineation.Type.INT_BIG_ENDIAN_FIELD, 30, 40, "")),
                transport -> transport.command(connectedEvent(), SendData.class).set(new byte[]{1, 2, 3, 4, 5, 6}, 205),
                transport -> set(transport.command(connectedEvent(), SendMessage.class), 0, new byte[]{1, 2, 3, 4, 5, 6}, 2, 3),
                transport -> transport.command(StopListening.class).set(204, 7788)
        );
    }

    public static Connected connectedEvent()
    {
        return new Connected(8881, 3, "remoteHost", 8882, 4, 56000, 80000, rawStreaming());
    }

    private static SendMessage set(final SendMessage command, final int index, byte[] src, final int offset, final int length)
    {
        command.prepare(length).putBytes(index, src, offset, length);
        command.commit();
        return command;
    }
}
