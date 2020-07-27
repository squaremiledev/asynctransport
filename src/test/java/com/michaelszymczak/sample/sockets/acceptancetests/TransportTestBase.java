package com.michaelszymczak.sample.sockets.acceptancetests;

import java.net.SocketException;
import java.util.function.BooleanSupplier;

import com.michaelszymczak.sample.sockets.domain.api.events.CommandFailed;
import com.michaelszymczak.sample.sockets.support.SampleClients;
import com.michaelszymczak.sample.sockets.support.TransportUnderTest;

import org.junit.jupiter.api.AfterEach;

import static org.assertj.core.api.Assertions.assertThat;


import static com.michaelszymczak.sample.sockets.support.TearDown.closeCleanly;
import static com.michaelszymczak.sample.sockets.support.Worker.runUntil;

abstract class TransportTestBase
{
    final TransportUnderTest serverTransport = new TransportUnderTest();
    final SampleClients clients = createSampleClient();
    final TransportUnderTest clientTransport = new TransportUnderTest();

    void spinUntil(final BooleanSupplier booleanSupplier)
    {
        runUntil(makeSureNoFailuresAndWorkUntil(booleanSupplier));
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

    private static SampleClients createSampleClient()
    {
        try
        {
            return new SampleClients();
        }
        catch (SocketException e)
        {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown()
    {
        clients.close();
        closeCleanly(serverTransport);
        closeCleanly(clientTransport);
    }
}
