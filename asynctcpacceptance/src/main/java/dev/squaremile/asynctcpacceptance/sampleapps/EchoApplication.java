package dev.squaremile.asynctcpacceptance.sampleapps;


import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.commands.StopListening;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.events.StoppedListening;
import dev.squaremile.asynctcp.transport.api.values.PredefinedTransportDelineation;

import static java.util.Objects.requireNonNull;

public class EchoApplication implements Application
{
    private final Transport transport;
    private final int listeningPort;
    private final EventListener eventListener;
    private final PredefinedTransportDelineation delineation;
    private boolean listening = false;
    private int nextCommandId = 101;

    public EchoApplication(final Transport transport, final int listeningPort, final EventListener eventListener, final PredefinedTransportDelineation delineation)
    {
        this(transport, listeningPort, eventListener, delineation, 101);
    }

    public EchoApplication(
            final Transport transport,
            final int listeningPort,
            final EventListener eventListener,
            final PredefinedTransportDelineation delineation,
            final int initialCommandId
    )
    {
        this.transport = requireNonNull(transport);
        this.listeningPort = listeningPort;
        this.eventListener = eventListener;
        this.delineation = delineation;
        this.nextCommandId = initialCommandId;
    }

    @Override
    public void onStart()
    {
        transport.handle(transport.command(Listen.class).set(nextCommandId++, listeningPort, delineation));
    }

    @Override
    public void onStop()
    {
        if (listening)
        {
            transport.handle(transport.command(StopListening.class).set(nextCommandId++, listeningPort));
        }
    }

    @Override
    public void onEvent(final Event event)
    {
        eventListener.onEvent(event);
        if (event instanceof MessageReceived)
        {
            transport.handle(sendDataCommandWithDataFrom((MessageReceived)event));
        }
        if (event instanceof StartedListening)
        {
            listening = true;
        }
        if (event instanceof StoppedListening)
        {
            listening = false;
        }
    }

    private SendData sendDataCommandWithDataFrom(final MessageReceived messageReceivedEvent)
    {
        SendData sendData = transport.command(messageReceivedEvent, SendData.class);
        messageReceivedEvent.buffer().getBytes(
                messageReceivedEvent.offset(), sendData.prepare(), 0, messageReceivedEvent.length());
        return sendData.commit(messageReceivedEvent.length());
    }

    @Override
    public void work()
    {

    }
}
