package dev.squaremile.transport.aeron;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import io.aeron.Aeron;
import io.aeron.ExclusivePublication;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

class AeronConnectionTest
{

    private MediaDriver mediaDriver;

    @BeforeEach
    void setUp()
    {
        mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context().threadingMode(ThreadingMode.SHARED));
    }

    @AfterEach
    void tearDown()
    {
        mediaDriver.close();
    }

    @Test
    void sandbox()
    {
        final AeronConnection aeronConnection = new AeronConnection(10, 11, mediaDriver.aeronDirectoryName());
        try (Aeron aeron = Aeron.connect(aeronConnection.aeronContext()))
        {
            Subscription toNetworkSubscription = aeron.addSubscription(aeronConnection.channel(), aeronConnection.toNetworAeronStreamId());
            Subscription fromNetworkSubscription = aeron.addSubscription(aeronConnection.channel(), aeronConnection.fromNetworAeronStreamId());

            ExclusivePublication toNetworkPublication = aeron.addExclusivePublication(aeronConnection.channel(), aeronConnection.toNetworAeronStreamId());
            ExclusivePublication fromNetworkPublication = aeron.addExclusivePublication(aeronConnection.channel(), aeronConnection.fromNetworAeronStreamId());

            while (!toNetworkPublication.isConnected())
            {

            }
            while (!fromNetworkPublication.isConnected())
            {

            }
        }
    }
}