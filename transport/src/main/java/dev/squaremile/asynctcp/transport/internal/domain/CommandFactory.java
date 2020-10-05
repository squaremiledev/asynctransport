package dev.squaremile.asynctcp.transport.internal.domain;

import dev.squaremile.asynctcp.transport.api.app.TransportCommand;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.StopListening;

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
