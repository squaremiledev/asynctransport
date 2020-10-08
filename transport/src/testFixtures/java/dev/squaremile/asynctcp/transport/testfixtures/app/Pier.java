package dev.squaremile.asynctcp.transport.testfixtures.app;

import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.app.TransportCorrelatedEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportEvent;
import dev.squaremile.asynctcp.transport.api.app.TransportOnDuty;
import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.testfixtures.Spy;

public class Pier implements TransportOnDuty
{

    private final TransportOnDuty transport;
    private final Spy<TransportEvent> eventsSpy;

    public Pier(final TransportOnDuty transport, final Spy<TransportEvent> eventsSpy)
    {
        this.transport = transport;
        this.eventsSpy = eventsSpy;
    }

    public Spy<TransportEvent> receivedEvents()
    {
        return eventsSpy;
    }

    public <T extends TransportCorrelatedEvent> T lastResponse(final Class<T> eventType, final int commandId)
    {
        return eventsSpy.last(eventType, event -> event.commandId() == commandId);
    }

    @Override
    public void close()
    {
        transport.close();
    }

    @Override
    public <C extends TransportUserCommand> C command(final Class<C> commandType)
    {
        return transport.command(commandType);
    }

    @Override
    public <C extends ConnectionUserCommand> C command(final ConnectionId connectionId, final Class<C> commandType)
    {
        return transport.command(connectionId, commandType);
    }

    @Override
    public void work()
    {
        transport.work();
    }

    @Override
    public void handle(final TransportCommand command)
    {
        transport.handle(command);
    }
}
