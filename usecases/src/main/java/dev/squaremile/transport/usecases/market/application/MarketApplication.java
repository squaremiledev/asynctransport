package dev.squaremile.transport.usecases.market.application;

import java.util.function.BooleanSupplier;

import org.agrona.collections.MutableBoolean;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.serialization.api.SerializedMessageListener;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

import static dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory.onStart;
import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.lengthBasedDelineation;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.Type.SHORT_LITTLE_ENDIAN_FIELD;
import static java.lang.System.currentTimeMillis;

public class MarketApplication
{
    private final int port;
    private final Runnable onReady;
    private final Runnable onConnected;
    private final MutableBoolean startedListening = new MutableBoolean(false);
    private final MutableBoolean connected = new MutableBoolean(false);
    private TransportApplicationOnDuty transportApplication;
    private ConnectionTransport connectionTransport;
    private ConnectionIdValue connectionId;

    public MarketApplication(final int port)
    {
        this.port = port;
        this.onReady = () -> startedListening.set(true);
        this.onConnected = () -> connected.set(true);
    }

    public TransportApplicationOnDuty startTransport(final BooleanSupplier returnWhen, final int timeoutMs)
    {
        if (transportApplication != null)
        {
            throw new IllegalStateException("Application already started");
        }
        final TransportApplicationOnDuty application = new AsyncTcp().create("marketApp", 1024, SerializedMessageListener.NO_OP, transport -> new ListeningApplication(
                transport,
                lengthBasedDelineation(SHORT_LITTLE_ENDIAN_FIELD, 0, 0),
                port,
                event ->
                {
                    if (event instanceof StartedListening)
                    {
                        onReady.run();
                    }
                },
                onStart((connectionTransport, connectionId) ->
                        {
                            this.connectionTransport = connectionTransport;
                            this.connectionId = new ConnectionIdValue(connectionId);
                            onConnected.run();
                            return this::onEvent;
                        })
        ));
        application.onStart();
        long startTime = currentTimeMillis();
        while (!returnWhen.getAsBoolean() && currentTimeMillis() < startTime + timeoutMs)
        {
            application.work();
        }
        if (!returnWhen.getAsBoolean())
        {
            throw new IllegalStateException("Unable to satisfy condition within " + timeoutMs + "ms.");
        }
        this.transportApplication = application;
        return application;
    }

    private void onEvent(final ConnectionEvent connectionEvent)
    {
        if (connectionEvent instanceof MessageReceived)
        {
            MessageReceived messageReceived = (MessageReceived)connectionEvent;
            SendMessage sendMessage = connectionTransport.command(SendMessage.class);
            sendMessage.prepare().putLong(sendMessage.offset(), messageReceived.buffer().getLong(messageReceived.offset()));
            sendMessage.commit(Long.BYTES);
            connectionTransport.handle(sendMessage);
        }
    }

    public boolean startedListening()
    {
        if (transportApplication != null)
        {
            transportApplication.work();
        }
        return startedListening.get();
    }

    public boolean acceptedMarketMakerConnection()
    {
        if (transportApplication != null)
        {
            transportApplication.work();
        }
        return connected.get();
    }
}
