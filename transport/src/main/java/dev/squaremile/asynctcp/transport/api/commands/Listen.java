package dev.squaremile.asynctcp.transport.api.commands;

import dev.squaremile.asynctcp.transport.api.app.TransportUserCommand;
import dev.squaremile.asynctcp.transport.api.values.CommandId;
import dev.squaremile.asynctcp.transport.api.values.DelineationType;
import dev.squaremile.asynctcp.transport.api.values.TransportId;

public class Listen implements TransportUserCommand
{
    private int port = TransportId.NO_PORT;
    private long commandId = CommandId.NO_COMMAND_ID;
    private DelineationType delineation;

    public Listen set(final long commandId, final int port, final DelineationType delineation)
    {
        this.port = port;
        this.commandId = commandId;
        this.delineation = delineation;
        return this;
    }

    @Override
    public int port()
    {
        return port;
    }

    @Override
    public long commandId()
    {
        return commandId;
    }

    public DelineationType delineation()
    {
        return delineation;
    }

    @Override
    public String toString()
    {
        return "Listen{" +
               "port=" + port +
               ", commandId=" + commandId +
               ", delineation=" + delineation +
               '}';
    }

    @Override
    public Listen copy()
    {
        return new Listen().set(commandId, port, delineation);
    }
}
