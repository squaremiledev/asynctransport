package dev.squaremile.asynctcp.api.transport.app;

public interface ConnectionCommandProvider
{
    <C extends ConnectionUserCommand> C command(Class<C> commandType);
}
