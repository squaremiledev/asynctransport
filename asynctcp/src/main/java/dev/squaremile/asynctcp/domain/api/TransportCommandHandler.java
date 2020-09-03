package dev.squaremile.asynctcp.domain.api;

import dev.squaremile.asynctcp.domain.api.commands.TransportCommand;

public interface TransportCommandHandler
{
    void handle(TransportCommand command);
}
