package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.concurrent.TimeUnit;

import org.agrona.collections.MutableLong;
import org.agrona.collections.MutableReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.application.TransportAppLauncher;
import dev.squaremile.asynctcp.application.TransportApplication;
import dev.squaremile.asynctcp.domain.api.StandardEncoding;
import dev.squaremile.asynctcp.domain.api.Transport;
import dev.squaremile.asynctcp.domain.api.commands.Connect;
import dev.squaremile.asynctcp.domain.api.commands.SendData;
import dev.squaremile.asynctcp.domain.api.events.Connected;
import dev.squaremile.asynctcp.domain.api.events.DataReceived;
import dev.squaremile.asynctcp.domain.api.events.TransportEventsListener;
import dev.squaremile.asynctcp.encodings.FixedLengthDataHandler;
import dev.squaremile.asynctcp.testfitures.Worker;
import dev.squaremile.asynctcp.testfitures.app.WhiteboxApplication;

import static dev.squaremile.asynctcp.domain.api.events.EventListener.IGNORE_EVENTS;
import static dev.squaremile.asynctcp.testfitures.FreePort.freePort;

class EchoApplicationThroughputTest
{
    private final TransportApplication drivingApplication;
    private final TransportApplication transportApplication;
    private final MutableReference<Connected> connectedEventHolder = new MutableReference<>();
    private final MutableLong totalBytesReceived = new MutableLong(0);
    private final MutableLong totalMessagesReceived = new MutableLong(0);
    private final int messageSize = 4 * 1024;
    private final FixedLengthDataHandler messageCounter = new FixedLengthDataHandler(messageReceived -> totalMessagesReceived.increment(), messageSize);
    private int port;
    private WhiteboxApplication<TransportEventsListener> whiteboxApplication;

    EchoApplicationThroughputTest()
    {
        drivingApplication = new TransportAppLauncher().launch(
                transport ->
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
                }, "");
        drivingApplication.onStart();
        port = freePort();
        transportApplication = new TransportAppLauncher().launch(transport -> new EchoApplication(transport, port, IGNORE_EVENTS, StandardEncoding.FOUR_KB), "");
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