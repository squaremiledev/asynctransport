package dev.squaremile.asynctcpacceptance;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.Connect;

import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.rawStreaming;
import static java.lang.Integer.parseInt;
import static java.util.Collections.singletonList;

public class AppConnectingToTcpPort implements EventDrivenApplication
{
    private final Transport transport;
    private final int port;

    private AppConnectingToTcpPort(final Transport transport, final int port)
    {
        this.transport = transport;
        this.port = port;
    }

    public static void main(String[] args)
    {
        if (args.length == 1)
        {
            ApplicationOnDuty transportApplication = new AsyncTcp()
                    .createSharedStack("", transport -> new AppConnectingToTcpPort(transport, parseInt(args[0])));

            new NaiveRoundRobinSingleThreadRunner().run(singletonList(transportApplication));
        }
        else
        {
            System.out.println("Provide a port to connect to");
        }
    }

    @Override
    public void onStart()
    {
        System.out.println("START");
        transport.handle(transport.command(Connect.class).set("localhost", port, (long)1, 1_000, rawStreaming()));
    }

    @Override
    public void onStop()
    {
        System.out.println("STOP");
    }

    @Override
    public void work()
    {

    }

    @Override
    public void onEvent(final Event event)
    {
        System.out.println(event);
    }
}
