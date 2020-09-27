package dev.squaremile.asynctcp.serialization.internal;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.stream.Stream;


import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.commands.StopListening;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionCommandFailed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.transport.api.events.DataSent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.events.StoppedListening;
import dev.squaremile.asynctcp.transport.api.events.TransportCommandFailed;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.api.values.PredefinedTransportEncoding;

class Fixtures
{
    /**
     * @return All events that can be processed by a separate component
     * <p>
     * Delineation process applied to DataReceived will result in zero or more MessageReceived events, thus
     * DataReceived event itself is not currently serialized.
     */
    static Stream<TransportEvent> serializableEvents()
    {
        return Stream.of(
                new Connected(8881, 3, "remoteHost", 8882, 4, 56000, 80000),
                new ConnectionAccepted(9881, 4, "remote", 9882, 5, 46000, 30000),
                new ConnectionClosed(7888, 1, 2),
                new ConnectionCommandFailed(8884, 103, "some details", 6),
                new ConnectionResetByPeer(5888, 4, 6),
                new DataSent(5888, 4, 1, 9, 18, 104),
                new MessageReceived(new ConnectionIdValue(8899, 4)).set(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7}), 5),
                new StartedListening(8888, 5),
                new StoppedListening(8988, 6),
                new TransportCommandFailed(8001, 101L, "some details", Listen.class)

        );
    }

    static Stream<Function<Transport, TransportUserCommand>> commands()
    {
        return Stream.of(
                transport -> transport.command(connectedEvent(), CloseConnection.class).set(201),
                transport -> transport.command(Connect.class).set("remoteHost", 8899, 202, 10, PredefinedTransportEncoding.SINGLE_BYTE),
                transport -> transport.command(Listen.class).set(203, 6688, PredefinedTransportEncoding.LONGS),
                transport -> transport.command(connectedEvent(), SendData.class).set(new byte[]{1, 2, 3, 4, 5, 6}, 205),
                transport -> transport.command(StopListening.class).set(204, 7788)
        );
    }

    public static Connected connectedEvent()
    {
        return new Connected(8881, 3, "remoteHost", 8882, 4, 56000, 80000);
    }
}
