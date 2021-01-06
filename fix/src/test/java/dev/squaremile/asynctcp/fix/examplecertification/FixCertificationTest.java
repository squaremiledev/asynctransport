package dev.squaremile.asynctcp.fix.examplecertification;

import java.util.HashSet;
import java.util.Set;

import org.agrona.collections.MutableInteger;
import org.agrona.collections.MutableLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.api.wiring.ConnectingApplication;
import dev.squaremile.asynctcp.fixtures.MessageLog;
import dev.squaremile.asynctcp.api.transport.app.ApplicationOnDuty;

import static dev.squaremile.asynctcp.fix.examplecertification.FixCertification.fixCertification;
import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.fixMessage;
import static dev.squaremile.asynctcp.fixtures.transport.FreePort.freePort;

public class FixCertificationTest
{
    private static final int TOTAL_MESSAGES_TO_RECEIVE = 1;
    private final MutableLong messageCount = new MutableLong();
    private final TransportApplicationFactory asyncTcp = new AsyncTcp();
    private final int port = freePort();
    private final MessageLog acceptorMessageLog = new MessageLog();

    @Test
    @Timeout(value = 5)
    void shouldPickCorrectFakeImplementationToConductCertification() throws InterruptedException
    {
        final ApplicationOnDuty certifyingApplication = fixCertification().start(port, acceptorMessageLog);
        final ApplicationOnDuty initiator = asyncTcp.createSharedStack("initiator", transport ->
                new ConnectingApplication(
                        transport,
                        "localhost",
                        port,
                        fixMessage(),
                        (connectionTransport, connectionId) -> new SendLogOn(
                                connectionTransport,
                                messageCount::increment,
                                connectionId,
                                TOTAL_MESSAGES_TO_RECEIVE,
                                FixCertification.USE_CASE_002_FIX11_REJECTED_LOGON.fixVersion(),
                                FixCertification.USE_CASE_002_FIX11_REJECTED_LOGON.username()
                        )
                )
        );

        // When
        initiator.onStart();
        while (messageCount.get() < TOTAL_MESSAGES_TO_RECEIVE)
        {
            Thread.sleep(1);
            certifyingApplication.work();
            initiator.work();
        }
        certifyingApplication.onStop();
        initiator.onStop();

        // Then
        acceptorReceivedCorrectMessages(acceptorMessageLog);
    }

    private void acceptorReceivedCorrectMessages(final MessageLog messageLog)
    {
        final Set<String> uniqueFixMessages = new HashSet<>(1);
        final MutableInteger fixMessagesCount = new MutableInteger(0);
        messageLog.readAll(new ReceivedFixMessagesHandler(
                fixMessage ->
                {
                    fixMessagesCount.increment();
                    uniqueFixMessages.add(fixMessage.toString());
                }));
        assertThat(uniqueFixMessages).hasSize(1);
        assertThat(fixMessagesCount.get()).isEqualTo(TOTAL_MESSAGES_TO_RECEIVE);
    }

}
