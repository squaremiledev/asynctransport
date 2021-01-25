package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.io.IOException;

import org.agrona.CloseHelper;
import org.agrona.collections.Int2ObjectHashMap;

import static org.agrona.CloseHelper.closeAll;


import dev.squaremile.asynctcp.api.transport.app.EventListener;
import dev.squaremile.asynctcp.internal.transport.domain.CommandFactory;

public class Servers implements AutoCloseable
{
    private final Int2ObjectHashMap<Server> listeningSocketsByPort = new Int2ObjectHashMap<>();
    private final RelativeClock relativeClock;

    public Servers(final RelativeClock relativeClock)
    {
        this.relativeClock = relativeClock;
    }

    public Server serverListeningOn(final int port)
    {
        return listeningSocketsByPort.get(port);
    }

    public boolean isListeningOn(final int port)
    {
        return listeningSocketsByPort.containsKey(port);
    }

    public void start(
            final String role,
            final int port,
            final long commandIdThatTriggeredListening,
            final ConnectionIdSource connectionIdSource,
            final EventListener eventListener,
            final CommandFactory commandFactory
    ) throws IOException
    {
        final Server server = new Server(role, port, commandIdThatTriggeredListening, relativeClock, connectionIdSource, eventListener, commandFactory);
        server.listen();
        listeningSocketsByPort.put(server.port(), server);
    }

    public void stop(final int port)
    {
        CloseHelper.close(listeningSocketsByPort.remove(port));
    }

    @Override
    public void close()
    {
        closeAll(listeningSocketsByPort.values());
    }
}
