package dev.squaremile.asynctcp.transport.api.events;

import dev.squaremile.asynctcp.transport.api.app.TransportCorrelatedEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import dev.squaremile.asynctcp.transport.api.values.DelineationType;

public class StartedListening implements TransportCorrelatedEvent
{
    private final int port;
    private final long commandId;
    private final DelineationType delineation;

    public StartedListening(final int port, final long commandId, final DelineationType delineation)
    {
        this.port = port;
        this.commandId = commandId;
        this.delineation = delineation;
    }

    @Override
    public int port()
    {
        return port;
    }

    public DelineationType delineation()
    {
        return delineation;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    @Override
    public String toString()
    {
        return "StartedListening{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", delineation=" + delineation +
               '}';
    }

    @Override
    public TransportEvent copy()
    {
        return new StartedListening(port, commandId, delineation);
    }
}
