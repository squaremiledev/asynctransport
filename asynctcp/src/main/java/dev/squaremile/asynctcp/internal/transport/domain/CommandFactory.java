package dev.squaremile.asynctcp.internal.transport.domain;

import dev.squaremile.asynctcp.api.transport.app.TransportCommand;
import dev.squaremile.asynctcp.api.transport.commands.Connect;
import dev.squaremile.asynctcp.api.transport.commands.Listen;
import dev.squaremile.asynctcp.api.transport.commands.StopListening;

public class CommandFactory
{
    public <C extends TransportCommand> C create(final Class<C> commandType)
    {
        if (commandType.equals(Listen.class))
        {
            return commandType.cast(new Listen());
        }
        if (commandType.equals(Connect.class))
        {
            return commandType.cast(new Connect());
        }
        if (commandType.equals(StopListening.class))
        {
            return commandType.cast(new StopListening());
        }
        throw new IllegalArgumentException(commandType.getSimpleName());
    }
}
