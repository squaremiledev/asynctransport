package dev.squaremile.asynctcp.fix;


import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

public class FixHandlerFactory
{
    public static ConnectionApplicationFactory createFixHandler(final FixHandler fixHandler)
    {
        return (connectionTransport, connectionId) -> event ->
        {
            if (event instanceof MessageReceived)
            {
                fixHandler.onMessage(connectionTransport, (MessageReceived)event);
            }
        };
    }


}
