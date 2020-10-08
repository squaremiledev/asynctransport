package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.internal.NonProdGradeTransportAppFactory;
import dev.squaremile.asynctcp.serialization.internal.delineation.DelineationApplication;
import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.values.Delineation;
import dev.squaremile.asynctcp.transport.testfixtures.Worker;

import static dev.squaremile.asynctcp.transport.api.app.EventListener.IGNORE_EVENTS;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.Type.FIXED_LENGTH;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static java.lang.String.format;

class EchoApplicationThroughputTest
{
    private static final int BYTES_CAP = 20_000_000;
    private static final int MESSAGE_SIZE_IN_BYTES = 4 * 1024;

    private final Application testDrivingTransportApplication;
    private final Application appUnderTest;
    private final int port;
    private final ThroughputTestDrivingApp testDrivingApp;


    EchoApplicationThroughputTest()
    {
        port = freePort();
        testDrivingApp = new ThroughputTestDrivingApp(port, new Delineation(FIXED_LENGTH, MESSAGE_SIZE_IN_BYTES, ""));
        testDrivingTransportApplication = new NonProdGradeTransportAppFactory().create("testDrivingApp", testDrivingApp);
        testDrivingTransportApplication.onStart();
        appUnderTest = new NonProdGradeTransportAppFactory().create(
                "appUnderTest",
                transport -> new DelineationApplication(new DelineationApplication(
                        new MessageEchoApplication(
                                transport,
                                port,
                                IGNORE_EVENTS,
                                new Delineation(FIXED_LENGTH, MESSAGE_SIZE_IN_BYTES, ""), 101
                        )))
        );
        appUnderTest.onStart();
        appUnderTest.work();
    }

    @Test
    void shouldEchoBackTheStream()
    {
        Transport drivingTransport = testDrivingApp.transport();
        testDrivingApp.app().onStart();
        Worker.runUntil(() ->
                        {
                            drivingTransport.work();
                            appUnderTest.work();
                            return testDrivingApp.connectedEvent() != null;
                        });
        Connected connected = testDrivingApp.connectedEvent();
        SendMessage sendMessageCommand = drivingTransport.command(connected, SendMessage.class);
        int numberOfMessagesSentDuringOneIteration = connected.outboundPduLimit() / MESSAGE_SIZE_IN_BYTES;

        long startTimeMs = System.currentTimeMillis();

        while (testDrivingApp.totalBytesReceived() < BYTES_CAP)
        {
            for (int i = 0; i < numberOfMessagesSentDuringOneIteration; i++)
            {
                // Not sure to what extent not setting all the data and leaving what's in the buffer affects the result
                sendMessageCommand.prepare().putLong(sendMessageCommand.offset(), i);
                sendMessageCommand.commit(MESSAGE_SIZE_IN_BYTES);
                drivingTransport.handle(sendMessageCommand);
                drivingTransport.work();
                appUnderTest.work();
                if (testDrivingApp.totalBytesReceived() >= BYTES_CAP)
                {
                    break;
                }
            }
        }

        long endTimeMs = System.currentTimeMillis();
        long bitsReceived = testDrivingApp.totalBytesReceived() * 8;
        long bytesReceived = testDrivingApp.totalBytesReceived();
        long messagesReceived = testDrivingApp.totalBytesReceived() / MESSAGE_SIZE_IN_BYTES;
        long timeElapsedMs = endTimeMs - startTimeMs;
        long _bps = (bitsReceived * TimeUnit.SECONDS.toMillis(1)) / timeElapsedMs;
        long _Mbps = _bps / 1_000_000;
        long _msgps = _bps / (MESSAGE_SIZE_IN_BYTES * 8);
        System.out.println(format("bps = %d", _bps));
        System.out.println(format("Mbps = %d", _Mbps));
        System.out.println(format("msg/s = %d", _msgps));
        System.out.println("---------------------------");
        System.out.println(format("message size  = %d B", MESSAGE_SIZE_IN_BYTES));
        System.out.println(format("bytes received  = %d B", bytesReceived));
        System.out.println(format("messages received = %d", messagesReceived));
        System.out.println(format("time elapsed = %d ms", timeElapsedMs));

        assertThat(_Mbps).isGreaterThan(8);
        assertThat(_msgps).isGreaterThan(2_000);
    }

    @AfterEach
    void tearDown()
    {
        testDrivingTransportApplication.onStop();
        testDrivingTransportApplication.work();
        appUnderTest.onStop();
        appUnderTest.work();
    }
}