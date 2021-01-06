package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import dev.squaremile.asynctcp.api.transport.app.ConnectionCommand;
import dev.squaremile.asynctcp.api.transport.app.ConnectionUserCommand;
import dev.squaremile.asynctcp.api.transport.values.Delineation;
import dev.squaremile.asynctcp.internal.transport.domain.connection.Channel;
import dev.squaremile.asynctcp.internal.transport.domain.connection.Connection;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionConfiguration;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionEventsListener;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ConnectionState;
import dev.squaremile.asynctcp.internal.transport.domain.connection.SingleConnectionEvents;
import dev.squaremile.asynctcp.internal.transport.domain.connection.ValidatedConnection;

public class ConnectionImpl implements AutoCloseable, Connection
{
    private final Connection delegate;

    ConnectionImpl(
            final ConnectionConfiguration configuration,
            final RelativeClock relativeClock,
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
                        relativeClock,
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

    @Override
    public void work()
    {
        delegate.work();
    }
}
