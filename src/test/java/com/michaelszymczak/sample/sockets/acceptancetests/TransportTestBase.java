package com.michaelszymczak.sample.sockets.acceptancetests;

import java.net.SocketException;

import com.michaelszymczak.sample.sockets.support.SampleClients;
import com.michaelszymczak.sample.sockets.support.TransportUnderTest;

import org.junit.jupiter.api.AfterEach;


import static com.michaelszymczak.sample.sockets.support.TearDown.closeCleanly;

abstract class TransportTestBase
{
    final TransportUnderTest serverTransport = new TransportUnderTest();
    final SampleClients clients = createSampleClient();
    final TransportUnderTest clientTransport = new TransportUnderTest();

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
