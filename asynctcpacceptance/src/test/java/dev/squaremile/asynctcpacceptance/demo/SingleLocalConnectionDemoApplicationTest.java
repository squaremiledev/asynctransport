package dev.squaremile.asynctcpacceptance.demo;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

class SingleLocalConnectionDemoApplicationTest
{
    private static final Consumer<String> LOGGER = System.out::println;

    private final ApplicationLifecycle applicationLifecycle = new ApplicationLifecycle();

    @Test
    void printTheLifecycle()
    {
        ApplicationOnDuty app = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).createSharedStack(
                "singleLocalConnectionApplication",
                transport -> new SingleLocalConnectionDemoApplication(
                        transport,
                        new Delineation(Delineation.Type.FIXED_LENGTH, 0, 8, ""),
                        applicationLifecycle,
                        LOGGER,
                        freePort(),
                        (connectionTransport, connectionId) -> new LoggingConnectedDemoActor("Alice", connectionTransport, connectionId, LOGGER),
                        (connectionTransport, connectionId) -> new LoggingConnectedDemoActor("Bob", connectionTransport, connectionId, LOGGER)
                )
        );

        app.onStart();
        while (!applicationLifecycle.isUp())
        {
            app.work();
        }

        app.onStop();

        while (applicationLifecycle.isUp())
        {
            app.work();
        }
    }
}