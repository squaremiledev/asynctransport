package dev.squaremile.asynctcp.transport.api.app;

public interface TransportCommandHandler
{
    TransportCommandHandler NO_HANDLER = command ->
    {

    };

    void handle(TransportCommand command);
}
