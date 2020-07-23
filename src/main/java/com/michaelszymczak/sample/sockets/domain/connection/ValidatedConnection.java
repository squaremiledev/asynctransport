package com.michaelszymczak.sample.sockets.domain.connection;

import com.michaelszymczak.sample.sockets.domain.api.ConnectionIdValue;
import com.michaelszymczak.sample.sockets.domain.api.commands.ConnectionCommand;

public class ValidatedConnection implements AutoCloseable, Connection
{
    private final ConnectionIdValue connectionId;
    private final SingleConnectionEvents events;
    private Connection delegate;

    public ValidatedConnection(
            final ConnectionIdValue connectionId,
            final SingleConnectionEvents events,
            final Connection delegate
    )
    {
        this.connectionId = connectionId;
        this.events = events;
        this.delegate = delegate;
    }

    @Override
    public int port()
    {
        return delegate.port();
    }

    @Override
    public long connectionId()
    {
        return connectionId.connectionId();
    }

    @Override
    public boolean handle(final ConnectionCommand command)
    {
        return validate(command) && delegate.handle(command);
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

    private boolean validate(final ConnectionCommand command)
    {
        final String result = connectionId.validate(command);
        if (result != null)
        {
            events.commandFailed(command, result);
            return false;
        }
        return true;
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
