package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.concurrent.TimeUnit;

import org.agrona.collections.MutableLong;
import org.agrona.collections.MutableReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.app.TransportEventsListener;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.DataReceived;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.api.values.PredefinedTransportEncoding;
import dev.squaremile.asynctcp.transport.internal.transportencoding.FixedLengthDataHandler;
import dev.squaremile.asynctcp.transport.setup.TransportAppFactory;
import dev.squaremile.asynctcp.transport.setup.TransportApplication;
import dev.squaremile.asynctcp.transport.testfixtures.Worker;
import dev.squaremile.asynctcp.transport.testfixtures.app.WhiteboxApplication;

import static dev.squaremile.asynctcp.transport.api.app.EventListener.IGNORE_EVENTS;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

class EchoApplicationThroughputTest
{
    private final TransportApplication drivingApplication;
    private final TransportApplication transportApplication;
    private final MutableReference<Connected> connectedEventHolder = new MutableReference<>();
    private final MutableLong totalBytesReceived = new MutableLong(0);
    private final MutableLong totalMessagesReceived = new MutableLong(0);
    private final int messageSize = 4 * 1024;
    private final FixedLengthDataHandler messageCounter = new FixedLengthDataHandler(new ConnectionIdValue(8888, 1), messageReceived -> totalMessagesReceived.increment(), messageSize);
    private int port;
    private WhiteboxApplication<TransportEventsListener> whiteboxApplication;

    EchoApplicationThroughputTest()
    {
        drivingApplication = new TransportAppFactory().create(
                "", transport ->
                {
                    whiteboxApplication = new WhiteboxApplication<>(transport, event ->
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
                            messageCounter.onDataReceived(dataReceivedEvent);
                        }
                    });
                    return whiteboxApplication;
                });
        drivingApplication.onStart();
        port = freePort();
        transportApplication = new TransportAppFactory().create("", transport -> new EchoApplication(transport, port, IGNORE_EVENTS, PredefinedTransportEncoding.FOUR_KB));
        transportApplication.onStart();
        transportApplication.work();
    }

    @Test
    void shouldEchoBackTheStream()
    {
        Transport drivingTransport = whiteboxApplication.underlyingtTansport();
        drivingTransport.handle(drivingTransport.command(Connect.class).set("localhost", port, 1, 50));
        Worker.runUntil(() ->
                        {
                            drivingTransport.work();
                            transportApplication.work();
                            return connectedEventHolder.get() != null;
                        });
        Connected connected = connectedEventHolder.get();
        SendData sendDataCommand = drivingTransport.command(connected, SendData.class);
        int longsSentInOneGo = connected.outboundPduLimit() / messageSize;

        long startTimeMs = System.currentTimeMillis();
        Worker.runWithoutTimeoutUntil(() ->
                                      {
                                          for (int i = 0; i < longsSentInOneGo; i++)
                                          {
                                              sendDataCommand.prepare().putLong(i);
                                              sendDataCommand.commit(messageSize);
                                              drivingTransport.handle(sendDataCommand);
                                              drivingTransport.work();
                                              transportApplication.work();
                                          }
                                          return totalBytesReceived.get() > 200_000_000L;
                                      });
        long endTimeMs = System.currentTimeMillis();
        long bitsPerSecond = (totalBytesReceived.get() * TimeUnit.SECONDS.toMillis(1)) / (endTimeMs - startTimeMs);
        long messagesPerSecond = ((totalMessagesReceived.get()) * TimeUnit.SECONDS.toMillis(1)) / (endTimeMs - startTimeMs);
        long megabytesPerSecond = bitsPerSecond / 1024 / 1024;
        assertThat(megabytesPerSecond).isGreaterThan(1);
        assertThat(messagesPerSecond).isGreaterThan(4_000);
//        System.out.println(megabytesPerSecond);
//        System.out.println(messagesPerSecond);
    }

    @AfterEach
    void tearDown()
    {
        drivingApplication.onStop();
        drivingApplication.work();
        transportApplication.onStop();
        transportApplication.work();
    }
}