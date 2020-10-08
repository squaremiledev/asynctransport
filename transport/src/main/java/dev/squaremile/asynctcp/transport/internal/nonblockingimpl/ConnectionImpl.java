package dev.squaremile.asynctcp.transport.internal.nonblockingimpl;

import dev.squaremile.asynctcp.transport.api.app.ConnectionCommand;
import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.transport.api.values.Delineation;
import dev.squaremile.asynctcp.transport.internal.domain.connection.Channel;
import dev.squaremile.asynctcp.transport.internal.domain.connection.Connection;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionConfiguration;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionEventsListener;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ConnectionState;
import dev.squaremile.asynctcp.transport.internal.domain.connection.SingleConnectionEvents;
import dev.squaremile.asynctcp.transport.internal.domain.connection.ValidatedConnection;

public class ConnectionImpl implements AutoCloseable, Connection
{

    private Connection delegate;

    ConnectionImpl(
            final ConnectionConfiguration configuration,
            final Channel channel,
            final Delineation delineation,
            final ConnectionEventsListener eventsListener
    )
    {
        delegate = new ValidatedConnection(
                configuration.connectionId,
                new SingleConnectionEvents(
                        eventsListener,
                        configuration.connectionId.port(),
                        configuration.connectionId.connectionId(),
                        configuration.inboundPduLimit,
                        configuration.sendBufferSize
                ),
                new ChannelBackedConnection(
                        configuration,
                        channel,
                        delineation,
                        new SingleConnectionEvents(
                                eventsListener,
                                configuration.connectionId.port(),
                                configuration.connectionId.connectionId(),
                                configuration.inboundPduLimit,
                                configuration.sendBufferSize
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
