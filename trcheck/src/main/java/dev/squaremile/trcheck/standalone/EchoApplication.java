package dev.squaremile.trcheck.standalone;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.transport.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.events.StartedListening;
import dev.squaremile.asynctcp.api.transport.values.Delineation;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;

class EchoApplication implements ApplicationOnDuty
{
    private final ApplicationOnDuty applicationOnDuty;

    public EchoApplication(final int port, final Runnable onReady)
    {
        applicationOnDuty = new AsyncTcp().createSharedStack(
                "echo",
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

    public static void start(final int port, final Runnable onReady, final Runnable onShutDown)
    {
        final ApplicationOnDuty app = new EchoApplication(port, onReady);
        app.onStart();
        while (!Thread.interrupted())
        {
            app.work();
        }
        app.onStop();
        onShutDown.run();
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
