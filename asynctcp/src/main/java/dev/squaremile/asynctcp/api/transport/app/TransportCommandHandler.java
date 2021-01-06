package dev.squaremile.asynctcp.api.transport.app;

public interface TransportCommandHandler
{
    TransportCommandHandler NO_HANDLER = command ->
    {

    };

    void handle(TransportCommand command);
}
