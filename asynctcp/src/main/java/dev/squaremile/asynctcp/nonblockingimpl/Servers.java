package dev.squaremile.asynctcp.nonblockingimpl;

import java.io.IOException;

import org.agrona.CloseHelper;
import org.agrona.collections.Int2ObjectHashMap;

import static org.agrona.CloseHelper.closeAll;


import dev.squaremile.asynctcp.domain.api.commands.CommandFactory;
import dev.squaremile.asynctcp.domain.api.events.EventListener;
import dev.squaremile.asynctcp.encodings.StandardEncodingsAwareConnectionEventDelegates;

public class Servers implements AutoCloseable
{
    private final Int2ObjectHashMap<Server> listeningSocketsByPort = new Int2ObjectHashMap<>();
    private final StandardEncodingsAwareConnectionEventDelegates connectionEventDelegates;

    public Servers(final StandardEncodingsAwareConnectionEventDelegates connectionEventDelegates)
    {
        this.connectionEventDelegates = connectionEventDelegates;
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
            final int port,
            final long commandIdThatTriggeredListening,
            final String protocolName,
            final ConnectionIdSource connectionIdSource,
            final EventListener eventListener,
            final CommandFactory commandFactory
    ) throws IOException
    {
        final Server server = new Server(connectionEventDelegates, port, protocolName, commandIdThatTriggeredListening, connectionIdSource, eventListener, commandFactory);
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
