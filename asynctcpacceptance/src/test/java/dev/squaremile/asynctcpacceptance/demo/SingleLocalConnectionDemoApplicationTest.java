package dev.squaremile.asynctcpacceptance.demo;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.Application;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

class SingleLocalConnectionDemoApplicationTest
{
    private static final Consumer<String> LOGGER = System.out::println;

    private final ApplicationLifecycle applicationLifecycle = new ApplicationLifecycle();

    @Test
    void printTheLifecycle()
    {
        Application app = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
                "singleLocalConnectionApplication",
                transport -> new SingleLocalConnectionDemoApplication(
                        transport,
                        fixedLengthDelineation(8),
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