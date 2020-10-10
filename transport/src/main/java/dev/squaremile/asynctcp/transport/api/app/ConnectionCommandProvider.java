package dev.squaremile.asynctcp.transport.api.app;

public interface ConnectionCommandProvider
{
    <C extends ConnectionUserCommand> C command(Class<C> commandType);
}
