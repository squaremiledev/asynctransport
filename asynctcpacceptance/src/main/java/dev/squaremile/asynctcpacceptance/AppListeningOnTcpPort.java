package dev.squaremile.asynctcpacceptance;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.rawStreaming;
import static java.lang.Integer.parseInt;
import static java.util.Collections.singletonList;

public class AppListeningOnTcpPort
{
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Provide a port to listen on");
            return;
        }
        final int appPort = parseInt(args[0]);

        ApplicationOnDuty app = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
                "AppListeningOnTcpPort",
                transport -> new EventDrivenApplication()
                {
                    @Override
                    public void onStart()
                    {
                        System.out.println("START");
                        transport.handle(transport.command(Listen.class).set((long)1, appPort, rawStreaming()));
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
                        if (event instanceof ConnectionAccepted)
                        {
                            ConnectionAccepted connectionAccepted = (ConnectionAccepted)event;
                            transport.handle(transport.command(connectionAccepted.connectionId(), SendData.class).set(
                                    (
                                            "Let me tell you some details about this connection.\n" +
                                            "You are " + connectionAccepted.remoteHost() +
                                            " on port " + connectionAccepted.remotePort() +
                                            ". other details: " + connectionAccepted +
                                            "\n" +
                                            "Now you say something..." +
                                            "\n\n"
                                    ).getBytes()));

                        }
                        if (event instanceof MessageReceived)
                        {
                            MessageReceived dataReceived = (MessageReceived)event;
                            transport.handle(transport.command(dataReceived.connectionId(), SendData.class).set(
                                    (
                                            "You have sent me " + dataReceived.length() + " bytes\n"
                                    ).getBytes()));
                        }
                    }
                }
        );


        new NaiveRoundRobinSingleThreadRunner().run(singletonList(app));
    }
}
