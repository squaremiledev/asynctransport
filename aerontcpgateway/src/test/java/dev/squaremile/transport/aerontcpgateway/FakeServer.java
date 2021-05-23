package dev.squaremile.transport.aerontcpgateway;

import org.agrona.collections.MutableBoolean;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.commands.Listen;
import dev.squaremile.asynctcp.api.transport.events.StartedListening;
import dev.squaremile.asynctcp.support.transport.ThingsOnDutyRunner;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.support.transport.Worker.runUntil;

public class FakeServer
{
    public static TransportApplicationOnDuty startFakeServerListeningOn(final int port)
    {
        MutableBoolean testerStartedListening = new MutableBoolean(false);
        TransportApplicationOnDuty listeningApplication = new AsyncTcp().createSharedStack("tester", transport -> new TransportApplicationOnDuty()
        {
            @Override
            public void onStart()
            {
                transport.handle(transport.command(Listen.class).set(1, port, fixedLengthDelineation(Integer.BYTES)));
            }

            @Override
            public void onEvent(final Event event)
            {
                if (event instanceof StartedListening)
                {
                    StartedListening startedListening = (StartedListening)event;
                    if (startedListening.port() == port)
                    {
                        testerStartedListening.set(true);
                    }
                }
            }
        });
        listeningApplication.onStart();
        runUntil(new ThingsOnDutyRunner(listeningApplication).reached(testerStartedListening::get));
        return listeningApplication;
    }
}
