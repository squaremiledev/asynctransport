package dev.squaremile.asynctcp.transport.api.app;

public interface TransportCommandHandler
{
    void handle(TransportCommand command);
}
