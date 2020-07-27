package com.michaelszymczak.sample.sockets.nonblockingimpl;

import com.michaelszymczak.sample.sockets.domain.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.domain.connection.Channel;
import com.michaelszymczak.sample.sockets.domain.connection.Connection;
import com.michaelszymczak.sample.sockets.domain.connection.ConnectionConfiguration;
import com.michaelszymczak.sample.sockets.domain.connection.ConnectionEventsListener;
import com.michaelszymczak.sample.sockets.domain.connection.ConnectionState;
import com.michaelszymczak.sample.sockets.domain.connection.SingleConnectionEvents;
import com.michaelszymczak.sample.sockets.domain.connection.ValidatedConnection;

public class ConnectionImpl implements AutoCloseable, Connection
{

    private Connection delegate;

    ConnectionImpl(final ConnectionConfiguration configuration, final Channel channel, final ConnectionEventsListener eventsListener)
    {
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
    public <C extends ConnectionCommand> C command(final Class<C> commandType)
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
