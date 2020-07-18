package com.michaelszymczak.sample.sockets.connection;

import com.michaelszymczak.sample.sockets.api.ConnectionIdValue;
import com.michaelszymczak.sample.sockets.api.commands.CommandFactory;
import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.api.commands.NoOpCommand;
import com.michaelszymczak.sample.sockets.api.commands.ReadData;
import com.michaelszymczak.sample.sockets.api.commands.SendData;

public class ConnectionCommands
{
    private final SendData sendDataCommand;
    private final ReadData readDataCommand;
    private final NoOpCommand noOpCommand;

    public ConnectionCommands(final CommandFactory commandFactory, final ConnectionIdValue connectionId)
    {
        this.sendDataCommand = commandFactory.create(connectionId, SendData.class);
        this.readDataCommand = commandFactory.create(connectionId, ReadData.class);
        this.noOpCommand = commandFactory.create(connectionId, NoOpCommand.class);
    }

    public <C extends ConnectionCommand> C command(final Class<C> commandType)
    {
        if (commandType.equals(SendData.class))
        {
            return commandType.cast(sendDataCommand);
        }
        if (commandType.equals(ReadData.class))
        {
            return commandType.cast(readDataCommand);
        }
        if (commandType.equals(NoOpCommand.class))
        {
            return commandType.cast(noOpCommand);
        }
        throw new IllegalArgumentException(commandType.getSimpleName());
    }
}
