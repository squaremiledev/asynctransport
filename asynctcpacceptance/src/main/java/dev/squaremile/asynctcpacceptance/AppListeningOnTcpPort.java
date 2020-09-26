package dev.squaremile.asynctcpacceptance;

import dev.squaremile.asynctcp.api.app.Application;
import dev.squaremile.asynctcp.api.app.ApplicationFactory;
import dev.squaremile.asynctcp.api.app.Event;
import dev.squaremile.asynctcp.api.app.Transport;
import dev.squaremile.asynctcp.api.commands.Listen;
import dev.squaremile.asynctcp.setup.Setup;

import static java.lang.Integer.parseInt;

public class AppListeningOnTcpPort implements Application
{
    private final Transport transport;
    private int port;

    private AppListeningOnTcpPort(final Transport transport, final int port)
    {
        this.transport = transport;
        this.port = port;
    }

    public static void main(String[] args)
    {
        if (args.length == 1)
        {
            ApplicationFactory applicationFactory = transport -> new AppListeningOnTcpPort(transport, parseInt(args[0]));
            Setup.launchAsNaiveRoundRobinSingleThreadedApp(applicationFactory);
        }
        else
        {
            System.out.println("Provide a port to listen on");
        }
    }

    @Override
    public void onStart()
    {
        System.out.println("START");
        transport.handle(transport.command(Listen.class).set(1, port));
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
