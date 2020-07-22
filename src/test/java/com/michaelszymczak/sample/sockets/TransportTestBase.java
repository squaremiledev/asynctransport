package com.michaelszymczak.sample.sockets;

import java.net.SocketException;

import com.michaelszymczak.sample.sockets.support.SampleClients;
import com.michaelszymczak.sample.sockets.support.TransportUnderTest;

import org.junit.jupiter.api.AfterEach;


import static com.michaelszymczak.sample.sockets.support.TearDown.closeCleanly;

abstract class TransportTestBase
{
    final TransportUnderTest transport;
    final SampleClients clients;

    TransportTestBase()
    {
        transport = new TransportUnderTest();
        clients = createSampleClient();
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
        closeCleanly(transport, clients, transport.statusEvents());
    }
}
