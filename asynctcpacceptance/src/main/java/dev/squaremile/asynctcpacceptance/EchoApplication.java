package dev.squaremile.asynctcpacceptance;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static java.lang.Integer.parseInt;

public class EchoApplication implements ApplicationOnDuty
{
    private final ApplicationOnDuty applicationOnDuty;

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Usage: EchoApplication port");
            System.out.println("Example: EchoApplication 9998");
            return;
        }
        final ApplicationOnDuty app = new EchoApplication(parseInt(args[0]));
        app.onStart();
        while (!Thread.interrupted())
        {
            app.work();
        }
    }

    public EchoApplication(final int port)
    {
        applicationOnDuty = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).createSharedStack(
                "echo",
                transport -> new ListeningApplication(
                        transport,
                        new Delineation(Delineation.Type.INT_LITTLE_ENDIAN_FIELD, 0, 0, ""),
                        port,
                        EventListener.IGNORE_EVENTS,
                        EchoConnectionApplication::new
                )
        );
    }

    @Override
    public void onStart()
    {
        applicationOnDuty.onStart();
    }

    @Override
    public void onStop()
    {
        applicationOnDuty.onStop();
    }

    @Override
    public void work()
    {
        applicationOnDuty.work();
    }
}
