package dev.squaremile.asynctcp.transport.api.app;

public interface Transport extends TransportCommandHandler, OnDuty, AutoCloseable
{
    <C extends TransportUserCommand> C command(Class<C> commandType);

    <C extends ConnectionUserCommand> C command(long connectionId, Class<C> commandType);

    @Override
    void close();
}
