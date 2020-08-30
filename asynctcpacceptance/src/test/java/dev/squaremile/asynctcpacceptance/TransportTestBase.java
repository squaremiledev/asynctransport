package dev.squaremile.asynctcpacceptance;

import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.AfterEach;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.domain.api.events.CommandFailed;
import dev.squaremile.asynctcp.testfitures.TearDown;
import dev.squaremile.asynctcp.testfitures.TransportUnderTest;

import static dev.squaremile.asynctcp.testfitures.Worker.runUntil;

abstract class TransportTestBase
{
    final TransportUnderTest serverTransport = new TransportUnderTest();
    final SampleClients clients = new SampleClients();
    final TransportUnderTest clientTransport = new TransportUnderTest();

    void spinUntil(final BooleanSupplier booleanSupplier)
    {
        runUntil(makeSureNoFailuresAndWorkUntil(booleanSupplier));
    }

    void spinUntilFailure()
    {
        runUntil(() ->
                 {
                     System.out.println("clientTransport.events() = " + clientTransport.events());
                     serverTransport.work();
                     clientTransport.work();
                     return clientTransport.events().contains(CommandFailed.class) ||
                            serverTransport.events().contains(CommandFailed.class);
                 });
    }

    private BooleanSupplier makeSureNoFailuresAndWorkUntil(final BooleanSupplier terminationCriterion)
    {
        return () ->
        {
            serverTransport.work();
            clientTransport.work();
            assertThat(clientTransport.events().all(CommandFailed.class)).isEmpty();
            assertThat(serverTransport.events().all(CommandFailed.class)).isEmpty();
            return terminationCriterion.getAsBoolean();
        };
    }

    @AfterEach
    void tearDown()
    {
        clients.close();
        TearDown.closeCleanly(serverTransport);
        TearDown.closeCleanly(clientTransport);
    }
}
