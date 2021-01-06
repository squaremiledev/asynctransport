package dev.squaremile.asynctcpacceptance.demo;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.transport.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.commands.Listen;
import dev.squaremile.asynctcp.api.transport.commands.SendData;
import dev.squaremile.asynctcp.api.transport.events.ConnectionAccepted;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.rawStreaming;

public class AppFromReadme
{
    public static void main(String[] args)
    {
        ApplicationOnDuty app = new AsyncTcp().createSharedStack(
                "MyApp",
                transport -> new TransportApplicationOnDuty()
                {

                    @Override
                    public void onStart()
                    {
                        transport.handle(transport.command(Listen.class).set(1, 8889, rawStreaming()));
                        System.out.println("now you can run `telnet localhost 8889` in the terminal");
                    }

                    @Override
                    public void onStop()
                    {
                    }

                    @Override
                    public void onEvent(final Event event)
                    {
                        System.out.println(event);
                        if (event instanceof ConnectionAccepted)
                        {
                            ConnectionAccepted connectionAccepted = (ConnectionAccepted)event;
                            transport.handle(transport.command(connectionAccepted.connectionId(), SendData.class).set("Hi!".getBytes()));
                        }
                    }
                }
        );

        app.onStart();
        while (true)
        {
            app.work();
        }
    }
}
