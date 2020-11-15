package dev.squaremile.fix.certification;

import org.agrona.collections.MutableBoolean;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.serialization.internal.SerializedMessageListener;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.fix.FixAcceptorFactory;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;

public class Certification
{
    public static ApplicationOnDuty startCertifyingApplication(
            final int port,
            final int buffersSize,
            final SerializedMessageListener acceptorMessageLog,
            final FakeApplicationRepository fakeApplicationRepository
    )
    {
        final MutableBoolean readyToAcceptConnections = new MutableBoolean(false);
        final ApplicationOnDuty acceptor = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
                "acceptor",
                buffersSize,
                acceptorMessageLog,
                new FixAcceptorFactory(port, () -> readyToAcceptConnections.set(true), fakeApplicationRepository)
        );
        acceptor.onStart();
        while (!readyToAcceptConnections.get())
        {
            acceptor.work();
        }
        return acceptor;
    }
}
