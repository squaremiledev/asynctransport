package dev.squaremile.asynctcp.internal.nonblockingimpl;

import dev.squaremile.asynctcp.api.app.ConnectionCommand;
import dev.squaremile.asynctcp.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.internal.domain.connection.Channel;
import dev.squaremile.asynctcp.internal.domain.connection.Connection;
import dev.squaremile.asynctcp.internal.domain.connection.ConnectionConfiguration;
import dev.squaremile.asynctcp.internal.domain.connection.ConnectionEventsListener;
import dev.squaremile.asynctcp.internal.domain.connection.ConnectionState;
import dev.squaremile.asynctcp.internal.domain.connection.SingleConnectionEvents;
import dev.squaremile.asynctcp.internal.domain.connection.ValidatedConnection;

public class ConnectionImpl implements AutoCloseable, Connection
{

    private Connection delegate;

    ConnectionImpl(final ConnectionConfiguration configuration, final Channel channel, final ConnectionEventsListener eventsListener)
    {
//        System.out.println("C@" + configuration.connectionId);
        delegate = new ValidatedConnection(
                configuration.connectionId,
                new SingleConnectionEvents(
                        eventsListener,
                        configuration.connectionId.port(),
                        configuration.connectionId.connectionId(),
                        configuration.inboundPduLimit
                ),
                new ChannelBackedConnection(
                        configuration,
                        channel,
                        new SingleConnectionEvents(
                                eventsListener,
                                configuration.connectionId.port(),
                                configuration.connectionId.connectionId(),
                                configuration.inboundPduLimit
                        )
                )
        );
    }

    @Override
    public int port()
    {
        return delegate.port();
    }

    @Override
    public long connectionId()
    {
        return delegate.connectionId();
    }

    @Override
    public boolean handle(final ConnectionCommand command)
    {
        return delegate.handle(command);
    }

    @Override
    public <C extends ConnectionUserCommand> C command(final Class<C> commandType)
    {
        return delegate.command(commandType);
    }

    @Override
    public ConnectionState state()
    {
        return delegate.state();
    }

    @Override
    public void accepted(final long commandIdThatTriggeredListening)
    {
        delegate.accepted(commandIdThatTriggeredListening);
    }

    @Override
    public void connected(final long commandId)
    {
        delegate.connected(commandId);
    }

    @Override
    public void close() throws Exception
    {
        delegate.close();
    }

    @Override
    public String toString()
    {
        return delegate.toString();
    }

}
