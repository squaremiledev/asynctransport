package dev.squaremile.fix;

import java.util.HashSet;
import java.util.Set;

import org.agrona.collections.MutableInteger;
import org.agrona.collections.MutableLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.api.wiring.ConnectingApplication;
import dev.squaremile.asynctcp.fixtures.MessageLog;
import dev.squaremile.asynctcp.fixtures.TimingExtension;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.fix.certification.Certification;
import dev.squaremile.fix.certification.usecases.SampleUseCaseRepository;
import dev.squaremile.fix.certification.usecases.SampleUseCases;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.fixMessage;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

@ExtendWith(TimingExtension.class)
public class CertificationTest
{
    private static final int TOTAL_MESSAGES_TO_RECEIVE = 10_000;
    private final MutableLong messageCount = new MutableLong();
    private final TransportApplicationFactory transportApplicationFactory = new AsyncTcp().transportAppFactory(NON_PROD_GRADE);
    private final int port = freePort();
    private final MessageLog acceptorMessageLog = new MessageLog();

    @Test
    void shouldPickCorrectFakeImplementationToConductCertification()
    {
        // Given
        final ApplicationOnDuty certifyingApplication = Certification.startCertifyingApplication(
                port,
                1024 * 1024,
                acceptorMessageLog,
                new SampleUseCaseRepository()
        );

        final ApplicationOnDuty initiator = transportApplicationFactory.createSharedStack("initiator", transport ->
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
                                SampleUseCases.USE_CASE_002_REJECTED_LOGON.fixVersion(),
                                SampleUseCases.USE_CASE_002_REJECTED_LOGON.username()
                        )
                )
        );

        // When
        initiator.onStart();
        while (messageCount.get() < TOTAL_MESSAGES_TO_RECEIVE)
        {
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
