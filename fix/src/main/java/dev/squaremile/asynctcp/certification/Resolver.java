package dev.squaremile.asynctcp.certification;

import java.util.Optional;


import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

public interface Resolver<UseCase>
{
    Optional<UseCase> useCase(MessageReceived messageReceived);
}
