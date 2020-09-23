package dev.squaremile.asynctcp.testfixtures;

import dev.squaremile.asynctcp.api.app.TransportCommand;
import dev.squaremile.asynctcp.api.app.TransportCommandHandler;

public class TransportCommandSpy extends Spy<TransportCommand> implements TransportCommandHandler
{
    private final CapturedItems<TransportCommand> items;

    public TransportCommandSpy()
    {
        this(new CapturedItems<>());
    }

    private TransportCommandSpy(final CapturedItems<TransportCommand> items)
    {
        super(items);
        this.items = items;
    }

    @Override
    public void handle(final TransportCommand command)
    {
        items.add(command.copy());
    }
}
