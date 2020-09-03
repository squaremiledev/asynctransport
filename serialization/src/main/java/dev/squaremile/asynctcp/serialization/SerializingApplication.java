package dev.squaremile.asynctcp.serialization;

import java.nio.ByteBuffer;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.application.Application;
import dev.squaremile.asynctcp.domain.api.events.Connected;
import dev.squaremile.asynctcp.domain.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.domain.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.domain.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.domain.api.events.DataSent;
import dev.squaremile.asynctcp.domain.api.events.Event;
import dev.squaremile.asynctcp.domain.api.events.MessageReceived;
import dev.squaremile.asynctcp.domain.api.events.StartedListening;
import dev.squaremile.asynctcp.domain.api.events.TransportCommandFailed;
import dev.squaremile.asynctcp.sbe.ConnectedEncoder;
import dev.squaremile.asynctcp.sbe.ConnectionAcceptedEncoder;
import dev.squaremile.asynctcp.sbe.ConnectionClosedEncoder;
import dev.squaremile.asynctcp.sbe.ConnectionResetByPeerEncoder;
import dev.squaremile.asynctcp.sbe.DataSentEncoder;
import dev.squaremile.asynctcp.sbe.MessageHeaderEncoder;
import dev.squaremile.asynctcp.sbe.MessageReceivedEncoder;
import dev.squaremile.asynctcp.sbe.StartedListeningEncoder;
import dev.squaremile.asynctcp.sbe.TransportCommandFailedEncoder;
import dev.squaremile.asynctcp.sbe.VarDataEncodingEncoder;

public class SerializingApplication implements Application
{
    private final MutableDirectBuffer buffer;
    private final int offset;
    private final SerializedEventListener serializedEventListener;
    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final StartedListeningEncoder startedListeningEncoder = new StartedListeningEncoder();
    private final TransportCommandFailedEncoder transportCommandFailedEncoder = new TransportCommandFailedEncoder();
    private final ConnectedEncoder connectedEncoder = new ConnectedEncoder();
    private final ConnectionAcceptedEncoder connectionAcceptedEncoder = new ConnectionAcceptedEncoder();
    private final ConnectionClosedEncoder connectionClosedEncoder = new ConnectionClosedEncoder();
    private final ConnectionResetByPeerEncoder connectionResetByPeerEncoder = new ConnectionResetByPeerEncoder();
    private final DataSentEncoder dataSentEncoder = new DataSentEncoder();
    private final MessageReceivedEncoder messageReceivedEncoder = new MessageReceivedEncoder();


    public SerializingApplication(final MutableDirectBuffer buffer, final int offset, final SerializedEventListener serializedEventListener)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.serializedEventListener = serializedEventListener;
    }

    @Override
    public void onEvent(final Event unknownEvent)
    {
        if (unknownEvent instanceof StartedListening)
        {
            StartedListening event = (StartedListening)unknownEvent;
            startedListeningEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .commandId(event.commandId());
            serializedEventListener.onSerializedEvent(buffer, offset);
        }
        else if (unknownEvent instanceof TransportCommandFailed)
        {
            TransportCommandFailed event = (TransportCommandFailed)unknownEvent;
            transportCommandFailedEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .commandId(event.commandId())
                    .details(event.details())
                    .commandType(event.commandType());
            serializedEventListener.onSerializedEvent(buffer, offset);
        }
        else if (unknownEvent instanceof Connected)
        {
            Connected event = (Connected)unknownEvent;
            connectedEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .commandId(event.commandId())
                    .connectionId(event.connectionId())
                    .remotePort(event.remotePort())
                    .inboundPduLimit(event.inboundPduLimit())
                    .outboundPduLimit(event.outboundPduLimit())
                    .remoteHost(event.remoteHost());
            serializedEventListener.onSerializedEvent(buffer, offset);
        }
        else if (unknownEvent instanceof ConnectionAccepted)
        {
            ConnectionAccepted event = (ConnectionAccepted)unknownEvent;
            connectionAcceptedEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .commandId(event.commandId())
                    .connectionId(event.connectionId())
                    .remotePort(event.remotePort())
                    .inboundPduLimit(event.inboundPduLimit())
                    .outboundPduLimit(event.outboundPduLimit())
                    .remoteHost(event.remoteHost());
            serializedEventListener.onSerializedEvent(buffer, offset);
        }
        if (unknownEvent instanceof ConnectionClosed)
        {
            ConnectionClosed event = (ConnectionClosed)unknownEvent;
            connectionClosedEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .commandId(event.commandId())
                    .connectionId(event.connectionId());
            serializedEventListener.onSerializedEvent(buffer, offset);
        }
        if (unknownEvent instanceof ConnectionResetByPeer)
        {
            ConnectionResetByPeer event = (ConnectionResetByPeer)unknownEvent;
            connectionResetByPeerEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .commandId(event.commandId())
                    .connectionId(event.connectionId());
            serializedEventListener.onSerializedEvent(buffer, offset);
        }
        if (unknownEvent instanceof DataSent)
        {
            DataSent event = (DataSent)unknownEvent;
            dataSentEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .commandId(event.commandId())
                    .connectionId(event.connectionId())
                    .bytesSent(event.bytesSent())
                    .totalBytesSent(event.totalBytesSent())
                    .totalBytesBuffered(event.totalBytesBuffered());
            serializedEventListener.onSerializedEvent(buffer, offset);
        }
        if (unknownEvent instanceof MessageReceived)
        {
            MessageReceived event = (MessageReceived)unknownEvent;
            messageReceivedEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .connectionId(event.connectionId());
            VarDataEncodingEncoder dstData = messageReceivedEncoder.data();

            ByteBuffer srcBuffer = event.data();
            int srcLength = event.length();
            dstData.length(srcLength);
            int offset = dstData.offset();
            dstData.buffer().putBytes(offset + dstData.encodedLength(), srcBuffer, srcLength);

            serializedEventListener.onSerializedEvent(this.buffer, this.offset);
        }
    }

}
