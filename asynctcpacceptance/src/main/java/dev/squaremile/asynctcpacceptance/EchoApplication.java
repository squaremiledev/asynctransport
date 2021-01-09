package dev.squaremile.asynctcpacceptance;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.serialization.SerializedMessageListener;
import dev.squaremile.asynctcp.api.transport.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.events.StartedListening;
import dev.squaremile.asynctcp.api.transport.values.Delineation;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;

import static java.lang.Integer.parseInt;

public class EchoApplication implements ApplicationOnDuty
{
    private final ApplicationOnDuty applicationOnDuty;

    public EchoApplication(final int port, final Runnable onReady)
    {
        applicationOnDuty = new AsyncTcp().create(
                "echo",
                1024 * 1024,
                SerializedMessageListener.NO_OP,
                transport -> new ListeningApplication(
                        transport,
                        new Delineation(Delineation.Type.INT_LITTLE_ENDIAN_FIELD, 0, 0, ""),
                        port,
                        event ->
                        {
                            if (event instanceof StartedListening)
                            {
                                onReady.run();
                            }
                        },
                        (connectionTransport, connectionId) -> new EchoConnectionApplication(connectionTransport)
                )
        );
    }

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Usage: EchoApplication port");
            System.out.println("Example: EchoApplication 9998");
            return;
        }
        start(parseInt(args[0]), () ->
        {
        });
    }

    public static void start(final int port, final Runnable onReady)
    {
        final ApplicationOnDuty app = new EchoApplication(port, onReady);
        app.onStart();
        while (!Thread.interrupted())
        {
            app.work();
        }
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
