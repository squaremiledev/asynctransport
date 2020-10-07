package dev.squaremile.asynctcpacceptance;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.internal.TransportApplication;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.serialization.api.delineation.PredefinedTransportDelineation.RAW_STREAMING;
import static java.lang.Integer.parseInt;
import static java.util.Collections.singletonList;

public class AppConnectingToTcpPort implements Application
{
    private final Transport transport;
    private int port;

    private AppConnectingToTcpPort(final Transport transport, final int port)
    {
        this.transport = transport;
        this.port = port;
    }

    public static void main(String[] args)
    {
        if (args.length == 1)
        {
            TransportApplication transportApplication = new AsyncTcp().transportAppFactory(NON_PROD_GRADE)
                    .create("", transport -> new AppConnectingToTcpPort(transport, parseInt(args[0])));

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
        transport.handle(transport.command(Connect.class).set("localhost", port, (long)1, 1_000, RAW_STREAMING.type));
    }

    @Override
    public void onStop()
    {
        System.out.println("STOP");
    }

    @Override
    public void onEvent(final Event event)
    {
        System.out.println(event);
    }

    @Override
    public void work()
    {

    }
}
