package dev.squaremile.asynctcp.api.certification;

import java.util.Optional;


import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

public interface Resolver<T extends UseCase>
{
    Optional<T> useCase(MessageReceived messageReceived);
}
