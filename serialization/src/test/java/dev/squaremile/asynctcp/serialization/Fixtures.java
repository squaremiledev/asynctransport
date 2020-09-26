package dev.squaremile.asynctcp.serialization;

import java.nio.ByteBuffer;
import java.util.stream.Stream;


import dev.squaremile.asynctcp.api.app.TransportEvent;
import dev.squaremile.asynctcp.api.commands.Listen;
import dev.squaremile.asynctcp.api.events.Connected;
import dev.squaremile.asynctcp.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.api.events.ConnectionCommandFailed;
import dev.squaremile.asynctcp.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.api.events.DataSent;
import dev.squaremile.asynctcp.api.events.MessageReceived;
import dev.squaremile.asynctcp.api.events.StartedListening;
import dev.squaremile.asynctcp.api.events.StoppedListening;
import dev.squaremile.asynctcp.api.events.TransportCommandFailed;
import dev.squaremile.asynctcp.api.values.ConnectionIdValue;

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
}
