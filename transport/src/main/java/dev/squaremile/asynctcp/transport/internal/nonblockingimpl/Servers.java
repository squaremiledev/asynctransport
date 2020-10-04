package dev.squaremile.asynctcp.transport.internal.nonblockingimpl;

import java.io.IOException;

import org.agrona.CloseHelper;
import org.agrona.collections.Int2ObjectHashMap;

import static org.agrona.CloseHelper.closeAll;


import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.internal.domain.CommandFactory;

public class Servers implements AutoCloseable
{
    private final Int2ObjectHashMap<Server> listeningSocketsByPort = new Int2ObjectHashMap<>();

    public Server serverListeningOn(final int port)
    {
        return listeningSocketsByPort.get(port);
    }

    public boolean isListeningOn(final int port)
    {
        return listeningSocketsByPort.containsKey(port);
    }

    public void start(
            final int port,
            final long commandIdThatTriggeredListening,
            final ConnectionIdSource connectionIdSource,
            final EventListener eventListener,
            final CommandFactory commandFactory
    ) throws IOException
    {
        final Server server = new Server(port, commandIdThatTriggeredListening, connectionIdSource, eventListener, commandFactory);
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
