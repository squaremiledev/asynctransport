package dev.squaremile.asynctcpacceptance;

import java.io.IOException;


import dev.squaremile.asynctcp.api.app.Application;
import dev.squaremile.asynctcp.api.app.Transport;
import dev.squaremile.asynctcp.api.commands.Connect;
import dev.squaremile.asynctcp.api.app.Event;
import dev.squaremile.asynctcp.setup.Setup;

import static java.lang.Integer.parseInt;

public class AppConnectingToTcpPort implements Application
{
    private final Transport transport;
    private int port;

    private AppConnectingToTcpPort(final Transport transport, final int port)
    {
        this.transport = transport;
        this.port = port;
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length == 1)
        {
            Setup.launchAsNaiveRoundRobinSingleThreadedApp(
                    transport -> new AppConnectingToTcpPort(transport, parseInt(args[0])));
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
        transport.handle(transport.command(Connect.class).set("localhost", port, 1, 1_000));
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
}
