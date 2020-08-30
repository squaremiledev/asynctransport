package dev.squaremile.asynctcpacceptance;

import java.io.IOException;

import dev.squaremile.asynctcp.domain.api.ConnectionId;
import dev.squaremile.asynctcp.domain.api.Transport;
import dev.squaremile.asynctcp.domain.api.commands.ConnectionCommand;
import dev.squaremile.asynctcp.domain.api.commands.TransportCommand;
import dev.squaremile.asynctcp.domain.api.events.EventListener;
import dev.squaremile.asynctcp.nonblockingimpl.NonBlockingTransport;

public class SampleTransport implements Transport
{

    private final NonBlockingTransport transport;

    public SampleTransport(final EventListener eventListener) throws IOException
    {
        transport = new NonBlockingTransport(eventListener);
    }

    public static void main(String[] args) throws IOException
    {
        new SampleTransport(event ->
                            {

                            });
    }

    @Override
    public void work()
    {
        transport.work();
    }

    @Override
    public void handle(final TransportCommand command)
    {
        transport.handle(command);
    }

    @Override
    public void close()
    {
        transport.close();
    }

    @Override
    public <C extends TransportCommand> C command(final Class<C> commandType)
    {
        return transport.command(commandType);
    }

    @Override
    public <C extends ConnectionCommand> C command(
            final ConnectionId connectionId,
            final Class<C> commandType
    )
    {
        return transport.command(connectionId, commandType);
    }

}
