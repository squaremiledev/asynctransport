package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.concurrent.TimeUnit;

import org.agrona.collections.MutableLong;
import org.agrona.collections.MutableReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.serialization.api.delineation.FixedLengthDelineationType;
import dev.squaremile.asynctcp.serialization.internal.delineation.DelineationApplication;
import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.setup.TransportAppFactory;
import dev.squaremile.asynctcp.transport.setup.TransportApplication;
import dev.squaremile.asynctcp.transport.testfixtures.Worker;

import static dev.squaremile.asynctcp.serialization.api.delineation.PredefinedTransportDelineation.SINGLE_BYTE;
import static dev.squaremile.asynctcp.transport.api.app.EventListener.IGNORE_EVENTS;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static java.lang.String.format;

class EchoApplicationThroughputTest
{
    private static final int BYTES_CAP = 20_000_000;
    private static final int MESSAGE_SIZE_IN_BYTES = 4 * 1024;

    private final TransportApplication testDrivingApp;
    private final TransportApplication appUnderTest;
    private final MutableReference<Connected> connectedEventHolder = new MutableReference<>();
    private final MutableReference<Transport> transportHolder = new MutableReference<>();
    private final MutableLong totalBytesReceived = new MutableLong(0);
    private final int port = freePort();


    EchoApplicationThroughputTest()
    {
        testDrivingApp = new TransportAppFactory().create(
                "testDrivingApp",
                transport ->
                {
                    transportHolder.set(transport);
                    return new Application()
                    {
                        @Override
                        public void work()
                        {
                            transport.work();
                        }

                        @Override
                        public void onEvent(final Event event)
                        {
                            if (event instanceof Connected)
                            {
                                Connected connectedEvent = (Connected)event;
                                connectedEventHolder.set(connectedEvent.copy());
                            }
                            else if (event instanceof DataReceived)
                            {
                                DataReceived dataReceivedEvent = (DataReceived)event;
                                totalBytesReceived.set(dataReceivedEvent.totalBytesReceived());
                            }
                        }
                    };
                }
        );
        testDrivingApp.onStart();
        appUnderTest = new TransportAppFactory().create(
                "appUnderTest",
                transport -> new DelineationApplication(
                        new MessageEchoApplication(
                                transport,
                                port,
                                IGNORE_EVENTS,
                                new FixedLengthDelineationType(MESSAGE_SIZE_IN_BYTES), 101
                        ))
        );
        appUnderTest.onStart();
        appUnderTest.work();
    }

    @Test
    void shouldEchoBackTheStream()
    {
        Transport drivingTransport = transportHolder.get();
        drivingTransport.handle(drivingTransport.command(Connect.class).set("localhost", port, (long)1, 50, SINGLE_BYTE.type));
        Worker.runUntil(() ->
                        {
                            drivingTransport.work();
                            appUnderTest.work();
                            return connectedEventHolder.get() != null;
                        });
        Connected connected = connectedEventHolder.get();
        SendData sendDataCommand = drivingTransport.command(connected, SendData.class);
        int numberOfMessagesSentDuringOneIteration = connected.outboundPduLimit() / MESSAGE_SIZE_IN_BYTES;

        long startTimeMs = System.currentTimeMillis();
        Worker.runWithoutTimeoutUntil(() ->
                                      {
                                          for (int i = 0; i < numberOfMessagesSentDuringOneIteration; i++)
                                          {
                                              sendDataCommand.prepare().putLong(i);
                                              sendDataCommand.commit(MESSAGE_SIZE_IN_BYTES);
                                              drivingTransport.handle(sendDataCommand);
                                              drivingTransport.work();
                                              appUnderTest.work();
                                              if (totalBytesReceived.get() >= BYTES_CAP)
                                              {
                                                  break;
                                              }
                                          }
                                          return totalBytesReceived.get() >= BYTES_CAP;
                                      });
        long endTimeMs = System.currentTimeMillis();
        long bitsReceived = totalBytesReceived.get() * 8;
        long bytesReceived = totalBytesReceived.get();
        long messagesReceived = totalBytesReceived.get() / MESSAGE_SIZE_IN_BYTES;
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
        testDrivingApp.onStop();
        testDrivingApp.work();
        appUnderTest.onStop();
        appUnderTest.work();
    }
}