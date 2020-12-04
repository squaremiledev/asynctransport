package dev.squaremile.asynctcpacceptance;

import java.util.concurrent.TimeUnit;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.MutableBoolean;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.TransportApplicationFactory;
import dev.squaremile.asynctcp.api.wiring.ConnectingApplication;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.CommandFailed;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.DataSent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

import static dev.squaremile.asynctcp.serialization.api.SerializedMessageListener.NO_OP;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.CORRELATION_ID_OFFSET;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.EXTRA_DATA_OFFSET;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.NO_OPTIONS;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.OFFSET_OPTIONS;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.PLEASE_RESPOND_FLAG;
import static dev.squaremile.asynctcpacceptance.AdHocProtocol.SEND_TIME_OFFSET;
import static java.lang.Integer.parseInt;
import static java.lang.System.nanoTime;

public class SourcingConnectionApplication implements ConnectionApplication
{
    private final ConnectionTransport connectionTransport;
    private final int totalMessagesToSend;
    private final int requestToResponseRatio;
    private final MutableBoolean isDone;
    private final long messageDelayNs;
    private final OnMessageReceived onMessageReceived;
    private final SelectiveResponseRequest selectiveResponseRequest;
    private final byte[] extraData;
    long messagesSentCount = 0;
    long awaitingResponsesInFlight = 0;
    long messagesReceivedCount = 0;
    private long startedSendingTimestampNanos = Long.MIN_VALUE;

    public SourcingConnectionApplication(
            final ConnectionTransport connectionTransport,
            final int totalMessagesToSend,
            final MutableBoolean isDone,
            final int sendingRatePerSecond,
            final OnMessageReceived onMessageReceived,
            final int respondToEveryNthRequest,
            final int extraDataLength
    )
    {
        this.connectionTransport = connectionTransport;
        this.totalMessagesToSend = totalMessagesToSend;
        this.requestToResponseRatio = respondToEveryNthRequest;
        this.selectiveResponseRequest = new SelectiveResponseRequest(totalMessagesToSend, respondToEveryNthRequest);
        this.isDone = isDone;
        this.messageDelayNs = TimeUnit.SECONDS.toNanos(1) / sendingRatePerSecond;
        this.onMessageReceived = onMessageReceived;
        this.extraData = generateExtraData(extraDataLength);
    }

    private static byte[] generateExtraData(final int extraDataLength)
    {
        byte[] data = new byte[extraDataLength];
        for (int i = 0; i < data.length; i++)
        {
            data[i] = (byte)(i % 128);
        }
        return data;
    }

    public static void main(String[] args)
    {
        if (args.length != 8)
        {
            System.out.println("Usage: remoteHost remotePort sendingRatePerSecond skippedWarmUpResponses messagesSent respondOnlyToNthRequest useBuffers extraDataLength");
            System.out.println("e.g. localhost 8889 48000 400000 4000000 1 0 0");
            System.out.println("e.g. localhost 8889 48000 400000 4000000 2 1 32");
            return;
        }
        String remoteHost = args[0];
        final int remotePort = parseInt(args[1]);
        final int sendingRatePerSecond = parseInt(args[2]);
        final int skippedWarmUpResponses = parseInt(args[3]);
        final int messagesSent = parseInt(args[4]);
        final int respondToEveryNthRequest = parseInt(args[5]);
        final boolean useBuffers = parseInt(args[6]) == 1;
        final int extraDataLength = parseInt(args[7]);

        final int expectedResponses = messagesSent / respondToEveryNthRequest;
        final String description = String.format(
                "remoteHost %s, remotePort %d, sendingRatePerSecond %d, skippedWarmUpResponses %d , " +
                "messagesSent %d, %d expected responses with a response rate 1 for %d, use buffers: %s, extra data %d bytes",
                remoteHost, remotePort, sendingRatePerSecond, skippedWarmUpResponses,
                messagesSent, expectedResponses, respondToEveryNthRequest, useBuffers, extraDataLength
        );
        System.out.println("Starting with " + description);
        start(description, remoteHost, remotePort, sendingRatePerSecond, skippedWarmUpResponses, messagesSent, respondToEveryNthRequest, useBuffers, extraDataLength);
    }

    public static void start(
            final String description,
            final String remoteHost,
            final int remotePort,
            final int sendingRatePerSecond,
            final int skippedWarnUpResponses,
            final int messagesSent,
            final int respondToEveryNthRequest,
            final boolean useBuffers,
            final int extraDataLength
    )
    {
        int expectedResponses = messagesSent / respondToEveryNthRequest;
        if (skippedWarnUpResponses >= expectedResponses)
        {
            throw new IllegalArgumentException("All " + expectedResponses + " responses would be skipped");
        }
        final Measurements measurements = new Measurements(description, skippedWarnUpResponses + 1);
        final MutableBoolean isDone = new MutableBoolean(false);
        final ApplicationOnDuty source = createApplication(useBuffers, transport -> new ConnectingApplication(
                transport,
                remoteHost,
                remotePort,
                AdHocProtocol.DELINEATION,
                (connectionTransport, connectionId) -> new SourcingConnectionApplication(
                        connectionTransport,
                        messagesSent,
                        isDone,
                        sendingRatePerSecond,
                        measurements,
                        respondToEveryNthRequest,
                        extraDataLength
                )
        ));

        source.onStart();
        while (!isDone.get())
        {
            source.work();
        }
        source.onStop();

        measurements.printResults();
    }

    private static ApplicationOnDuty createApplication(final boolean useBuffers, final TransportApplicationOnDutyFactory applicationFactory)
    {
        final TransportApplicationFactory asyncTcp = new AsyncTcp();
        if (useBuffers)
        {
            System.out.println("Creating an app that uses ring buffers");
            return asyncTcp.create(
                    "source",
                    1024 * 1024,
                    NO_OP,
                    applicationFactory
            );
        }
        else
        {
            System.out.println("Creating an app without ring buffers");
            return asyncTcp.createSharedStack(
                    "source",
                    applicationFactory
            );
        }
    }

    @Override
    public void onStart()
    {
    }

    @Override
    public void work()
    {
        if (messagesSentCount < totalMessagesToSend)
        {
            final long nowNs = nanoTime();
            final long expectedTimestampNsToSendThisMessage;
            if (startedSendingTimestampNanos != Long.MIN_VALUE)
            {
                expectedTimestampNsToSendThisMessage = startedSendingTimestampNanos + messagesSentCount * messageDelayNs;
            }
            else
            {
                expectedTimestampNsToSendThisMessage = nowNs;
                startedSendingTimestampNanos = nowNs;
            }
            if (nowNs >= expectedTimestampNsToSendThisMessage)
            {
                boolean askToRespond = selectiveResponseRequest.shouldRespond(messagesSentCount);
                send(expectedTimestampNsToSendThisMessage, askToRespond, messagesSentCount);
                messagesSentCount++;
                awaitingResponsesInFlight += askToRespond ? 1 : 0;
            }
        }
    }

    @Override
    public void onEvent(final ConnectionEvent event)
    {
        if (event instanceof CommandFailed)
        {
            throw new IllegalStateException(((CommandFailed)event).details());
        }
        else if (event instanceof DataSent)
        {
            DataSent dataSent = (DataSent)event;
            if (dataSent.windowSizeInBytes() != dataSent.originalWindowSizeInBytes())
            {
                System.out.println("Window size changed to " + dataSent.windowSizeInBytes() + " bytes");
            }
        }
        else if (event instanceof MessageReceived)
        {
            long receivedTimeNs = nanoTime();
            messagesReceivedCount++;
            awaitingResponsesInFlight--;
            MessageReceived messageReceived = (MessageReceived)event;
            DirectBuffer buffer = messageReceived.buffer();
            long sendTimeNs = buffer.getLong(messageReceived.offset() + SEND_TIME_OFFSET);
            validateResponse(buffer.getLong(messageReceived.offset() + CORRELATION_ID_OFFSET));
            onMessageReceived.onMessageReceived(messagesSentCount, messagesReceivedCount, sendTimeNs, receivedTimeNs);
            if (selectiveResponseRequest.receivedLast(messagesReceivedCount))
            {
                isDone.set(true);
                if (awaitingResponsesInFlight != 0)
                {
                    throw new IllegalStateException("At this point we should have received all expected responses, " +
                                                    "but " + awaitingResponsesInFlight + " are still in flight");
                }
            }
        }
    }

    private void validateResponse(final long correlationId)
    {
        if (((messagesReceivedCount - 1) * requestToResponseRatio) != correlationId)
        {
            throw new IllegalStateException("A mismatch detected");
        }
    }

    private void send(final long supposedSendingTimestampNs, final boolean expectResponse, final long correlationId)
    {
        SendMessage message = connectionTransport.command(SendMessage.class);
        MutableDirectBuffer buffer = message.prepare();
        buffer.putInt(message.offset() + OFFSET_OPTIONS, expectResponse ? PLEASE_RESPOND_FLAG : NO_OPTIONS);
        buffer.putLong(message.offset() + SEND_TIME_OFFSET, supposedSendingTimestampNs);
        buffer.putLong(message.offset() + CORRELATION_ID_OFFSET, correlationId);
        buffer.putBytes(message.offset() + EXTRA_DATA_OFFSET, extraData);
        message.commit(EXTRA_DATA_OFFSET + extraData.length);
        connectionTransport.handle(message);
    }

    public interface OnMessageReceived
    {
        void onMessageReceived(long messagesSentCount, long messagesReceivedCount, long messageSentTimeNs, long messageReceivedTimeNs);
    }

}
