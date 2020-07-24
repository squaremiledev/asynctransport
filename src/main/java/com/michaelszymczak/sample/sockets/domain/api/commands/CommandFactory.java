package com.michaelszymczak.sample.sockets.domain.api.commands;

import com.michaelszymczak.sample.sockets.domain.api.ConnectionId;

public class CommandFactory
{
    public <C extends ConnectionCommand> C create(final ConnectionId connectionId, final Class<C> commandType)
    {
        if (commandType.equals(ReadData.class))
        {
            return commandType.cast(new ReadData(connectionId));
        }
        if (commandType.equals(SendData.class))
        {
            return commandType.cast(new SendData(connectionId, 0));
        }
        if (commandType.equals(NoOpCommand.class))
        {
            return commandType.cast(new NoOpCommand(connectionId));
        }
        throw new IllegalArgumentException(commandType.getSimpleName());
    }

    public <C extends TransportCommand> C create(final Class<C> commandType)
    {
        if (commandType.equals(Listen.class))
        {
            return commandType.cast(new Listen());
        }
        if (commandType.equals(CloseConnection.class))
        {
            return commandType.cast(new CloseConnection());
        }
        if (commandType.equals(StopListening.class))
        {
            return commandType.cast(new StopListening());
        }
        throw new IllegalArgumentException(commandType.getSimpleName());
    }
}