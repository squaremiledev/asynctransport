package dev.squaremile.asynctcp.api.certification;

import java.util.Optional;


import dev.squaremile.asynctcp.api.transport.events.MessageReceived;

public interface UseCases
{
    Optional<UseCase> useCase(MessageReceived messageReceived);
}
