package com.michaelszymczak.sample.sockets.domain.connection;

import com.michaelszymczak.sample.sockets.domain.api.ConnectionIdValue;
import com.michaelszymczak.sample.sockets.domain.api.commands.CommandFactory;
import com.michaelszymczak.sample.sockets.domain.api.commands.ConnectionCommand;
import com.michaelszymczak.sample.sockets.domain.api.commands.NoOpCommand;
import com.michaelszymczak.sample.sockets.domain.api.commands.ReadData;
import com.michaelszymczak.sample.sockets.domain.api.commands.SendData;

public class ConnectionCommands
{
    private final SendData sendDataCommand;
    private final ReadData readDataCommand;
    private final NoOpCommand noOpCommand;

    public ConnectionCommands(final ConnectionIdValue connectionId, final int initialSenderBufferSize)
    {
        CommandFactory commandFactory = new CommandFactory();
        this.sendDataCommand = new SendData(connectionId, initialSenderBufferSize);
        this.readDataCommand = commandFactory.create(connectionId, ReadData.class);
        this.noOpCommand = commandFactory.create(connectionId, NoOpCommand.class);
    }

    public <C extends ConnectionCommand> C command(final Class<C> commandType)
    {
        if (commandType.equals(SendData.class))
        {
            return commandType.cast(sendDataCommand.reset());
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
