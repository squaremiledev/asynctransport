package dev.squaremile.asynctcp.serialization.internal;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.sbe.ConnectedEncoder;
import dev.squaremile.asynctcp.sbe.ConnectionAcceptedEncoder;
import dev.squaremile.asynctcp.sbe.ConnectionClosedEncoder;
import dev.squaremile.asynctcp.sbe.ConnectionCommandFailedEncoder;
import dev.squaremile.asynctcp.sbe.ConnectionResetByPeerEncoder;
import dev.squaremile.asynctcp.sbe.DataSentEncoder;
import dev.squaremile.asynctcp.sbe.MessageHeaderEncoder;
import dev.squaremile.asynctcp.sbe.MessageReceivedEncoder;
import dev.squaremile.asynctcp.sbe.StartedListeningEncoder;
import dev.squaremile.asynctcp.sbe.StoppedListeningEncoder;
import dev.squaremile.asynctcp.sbe.TransportCommandFailedEncoder;
import dev.squaremile.asynctcp.sbe.VarDataEncodingEncoder;
import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionCommandFailed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.transport.api.events.DataSent;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.events.StoppedListening;
import dev.squaremile.asynctcp.transport.api.events.TransportCommandFailed;

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
    private final ConnectionCommandFailedEncoder connectionCommandFailedEncoder = new ConnectionCommandFailedEncoder();
    private final ConnectionResetByPeerEncoder connectionResetByPeerEncoder = new ConnectionResetByPeerEncoder();
    private final DataSentEncoder dataSentEncoder = new DataSentEncoder();
    private final MessageReceivedEncoder messageReceivedEncoder = new MessageReceivedEncoder();
    private final StoppedListeningEncoder stoppedListeningEncoder = new StoppedListeningEncoder();


    public SerializingApplication(final MutableDirectBuffer buffer, final int offset, final SerializedEventListener serializedEventListener)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.serializedEventListener = serializedEventListener;
    }

    @Override
    public void onEvent(final Event unknownEvent)
    {
        if (unknownEvent instanceof TransportCommandFailed)
        {
            TransportCommandFailed event = (TransportCommandFailed)unknownEvent;
            transportCommandFailedEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder);
            transportCommandFailedEncoder.port(event.port());

            transportCommandFailedEncoder.commandId(event.commandId());
            transportCommandFailedEncoder.details(event.details());
            transportCommandFailedEncoder.commandType(event.commandType());

            serializedEventListener.onSerialized(buffer, offset, headerEncoder.encodedLength() + transportCommandFailedEncoder.encodedLength());
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
            serializedEventListener.onSerialized(buffer, offset, headerEncoder.encodedLength() + connectedEncoder.encodedLength());
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
            serializedEventListener.onSerialized(buffer, offset, headerEncoder.encodedLength() + connectionAcceptedEncoder.encodedLength());
        }
        else if (unknownEvent instanceof ConnectionClosed)
        {
            ConnectionClosed event = (ConnectionClosed)unknownEvent;
            connectionClosedEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .commandId(event.commandId())
                    .connectionId(event.connectionId());
            serializedEventListener.onSerialized(buffer, offset, headerEncoder.encodedLength() + connectionClosedEncoder.encodedLength());
        }
        else if (unknownEvent instanceof ConnectionCommandFailed)
        {
            ConnectionCommandFailed event = (ConnectionCommandFailed)unknownEvent;
            connectionCommandFailedEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .commandId(event.commandId())
                    .connectionId(event.connectionId())
                    .details(event.details());
            serializedEventListener.onSerialized(buffer, offset, headerEncoder.encodedLength() + connectionCommandFailedEncoder.encodedLength());
        }
        else if (unknownEvent instanceof ConnectionResetByPeer)
        {
            ConnectionResetByPeer event = (ConnectionResetByPeer)unknownEvent;
            connectionResetByPeerEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .commandId(event.commandId())
                    .connectionId(event.connectionId());
            serializedEventListener.onSerialized(buffer, offset, headerEncoder.encodedLength() + connectionResetByPeerEncoder.encodedLength());
        }
        else if (unknownEvent instanceof DataSent)
        {
            DataSent event = (DataSent)unknownEvent;
            dataSentEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .commandId(event.commandId())
                    .connectionId(event.connectionId())
                    .bytesSent(event.bytesSent())
                    .totalBytesSent(event.totalBytesSent())
                    .totalBytesBuffered(event.totalBytesBuffered());
            serializedEventListener.onSerialized(buffer, offset, headerEncoder.encodedLength() + dataSentEncoder.encodedLength());
        }
        else if (unknownEvent instanceof StartedListening)
        {
            StartedListening event = (StartedListening)unknownEvent;
            startedListeningEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .commandId(event.commandId())
                    .delineation("" + event.delineation().fixedLength());
            serializedEventListener.onSerialized(buffer, offset, headerEncoder.encodedLength() + startedListeningEncoder.encodedLength());
        }

        else if (unknownEvent instanceof StoppedListening)
        {
            StoppedListening event = (StoppedListening)unknownEvent;
            stoppedListeningEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .commandId(event.commandId());
            serializedEventListener.onSerialized(this.buffer, this.offset, headerEncoder.encodedLength() + stoppedListeningEncoder.encodedLength());
        }
        else if (unknownEvent instanceof MessageReceived)
        {
            MessageReceived event = (MessageReceived)unknownEvent;
            messageReceivedEncoder.wrapAndApplyHeader(buffer, offset, headerEncoder)
                    .port(event.port())
                    .connectionId(event.connectionId());
            VarDataEncodingEncoder dstData = messageReceivedEncoder.data();

            DirectBuffer srcBuffer = event.buffer();
            int srcLength = event.length();
            dstData.length(srcLength);
            int offset = dstData.offset();
            dstData.buffer().putBytes(offset + dstData.encodedLength(), srcBuffer, event.offset(), srcLength);

            serializedEventListener.onSerialized(this.buffer, this.offset, headerEncoder.encodedLength() + messageReceivedEncoder.encodedLength());
        }
    }

    @Override
    public void work()
    {

    }
}
