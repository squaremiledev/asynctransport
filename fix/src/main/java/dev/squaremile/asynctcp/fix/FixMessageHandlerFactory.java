package dev.squaremile.asynctcp.fix;


import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

public class FixMessageHandlerFactory
{
    public static ConnectionApplicationFactory createFixMessageHandler(final FixMessageHandler fixMessageHandler)
    {
        return (connectionTransport, connectionId) -> event ->
        {
            if (event instanceof MessageReceived)
            {
                fixMessageHandler.onMessage(connectionTransport, (MessageReceived)event);
            }
        };
    }


}
