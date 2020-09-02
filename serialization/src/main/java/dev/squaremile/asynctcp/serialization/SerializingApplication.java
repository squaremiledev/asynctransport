package dev.squaremile.asynctcp.serialization;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.application.Application;
import dev.squaremile.asynctcp.domain.api.events.Event;
import dev.squaremile.asynctcp.domain.api.events.StartedListening;
import dev.squaremile.asynctcp.sbe.MessageHeaderEncoder;
import dev.squaremile.asynctcp.sbe.StartedListeningEncoder;

public class SerializingApplication implements Application
{
    private final MutableDirectBuffer buffer;
    private final int offset;
    private final SerializedEventListener serializedEventListener;
    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final StartedListeningEncoder startedListeningEncoder = new StartedListeningEncoder();

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
    }

}
