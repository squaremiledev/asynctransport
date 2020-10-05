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
import dev.squaremile.asynctcp.transport.setup.TransportAppFactory;
import dev.squaremile.asynctcp.transport.setup.TransportApplication;
import dev.squaremile.asynctcp.transport.testfixtures.Worker;
import dev.squaremile.asynctcp.transport.testfixtures.app.WhiteboxApplication;

import static dev.squaremile.asynctcp.serialization.api.delineation.PredefinedTransportDelineation.RAW_STREAMING;
import static dev.squaremile.asynctcp.transport.api.app.EventListener.IGNORE_EVENTS;
import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;
import static java.lang.String.format;

class EchoApplicationThroughputTest
{
    private static final int BYTES_CAP = 20_000_000;
    private static final int MESSAGE_SIZE_IN_BYTES = 4 * 1024;

    private final TransportApplication drivingApplication;
    private final TransportApplication transportApplication;
    private final MutableReference<Connected> connectedEventHolder = new MutableReference<>();
    private final MutableLong totalBytesReceived = new MutableLong(0);
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
                        }
                    });
                    return whiteboxApplication;
                });
        drivingApplication.onStart();
        port = freePort();
        transportApplication = new TransportAppFactory().create("", transport -> new EchoApplication(transport, port, IGNORE_EVENTS, 101));
        transportApplication.onStart();
        transportApplication.work();
    }

    @Test
    void shouldEchoBackTheStream()
    {
        Transport drivingTransport = whiteboxApplication.underlyingTransport();
        drivingTransport.handle(drivingTransport.command(Connect.class).set("localhost", port, (long)1, 50, RAW_STREAMING.type));
        Worker.runUntil(() ->
                        {
                            drivingTransport.work();
                            transportApplication.work();
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
                                              transportApplication.work();
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
        drivingApplication.onStop();
        drivingApplication.work();
        transportApplication.onStop();
        transportApplication.work();
    }
}