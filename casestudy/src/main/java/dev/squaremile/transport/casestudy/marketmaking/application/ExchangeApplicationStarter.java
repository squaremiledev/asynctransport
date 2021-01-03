package dev.squaremile.transport.casestudy.marketmaking.application;

import org.agrona.collections.MutableBoolean;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.serialization.api.SerializedMessageListener;
import dev.squaremile.asynctcp.transport.api.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.transport.casestudy.marketmaking.domain.Exchange;
import dev.squaremile.transport.casestudy.marketmaking.domain.MarketListener;
import dev.squaremile.transport.casestudy.marketmaking.domain.MidPriceUpdate;
import dev.squaremile.transport.casestudy.marketmaking.domain.TrackedSecurity;

import static dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory.onStart;
import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.lengthBasedDelineation;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.Type.SHORT_LITTLE_ENDIAN_FIELD;
import static dev.squaremile.transport.casestudy.marketmaking.domain.CurrentTime.currentTime;
import static dev.squaremile.transport.casestudy.marketmaking.domain.CurrentTime.timeFromMs;
import static dev.squaremile.transport.casestudy.marketmaking.domain.MarketListener.marketListeners;

public class ExchangeApplicationStarter
{
    private final int port;
    private final Clock clock;
    private final MarketListener marketListener;
    private final long tickCoolDownTime;
    private final MidPriceUpdate priceMovement;
    private final int initialMidPrice;
    private TransportApplicationOnDuty transportApplication;
    private MarketConnectionApplication<ExchangeApplication> marketConnectionApplication;

    public ExchangeApplicationStarter(
            final int port,
            final Clock clock,
            final long tickCoolDownTime,
            final MidPriceUpdate priceMovement,
            final int initialMidPrice,
            final MarketListener marketListener
    )
    {
        this.port = port;
        this.clock = clock;
        this.tickCoolDownTime = tickCoolDownTime;
        this.priceMovement = priceMovement;
        this.initialMidPrice = initialMidPrice;
        this.marketListener = marketListener;
    }

    public TransportApplicationOnDuty startTransport(final int timeoutMs)
    {
        if (transportApplication != null)
        {
            throw new IllegalStateException("Application already started");
        }
        final MutableBoolean startedListening = new MutableBoolean(false);
        final TransportApplicationOnDuty application = new AsyncTcp().create("marketApp", 128 * 1024, SerializedMessageListener.NO_OP, transport ->
        {
            final MarketParticipants marketParticipants = new MarketParticipants();
            MarketListener marketListener = marketListeners(
                    new MarketEventsPublisher(transport, marketParticipants),
                    this.marketListener
            );
            final Exchange exchange = new Exchange(new TrackedSecurity().midPrice(0, initialMidPrice), priceMovement, tickCoolDownTime, marketListener);
            return new ListeningApplication(
                    transport,
                    lengthBasedDelineation(SHORT_LITTLE_ENDIAN_FIELD, 0, 0),
                    port,
                    event ->
                    {
                        if (event instanceof StartedListening)
                        {
                            startedListening.set(true);
                        }
                    },
                    onStart((connectionTransport, connectionId) ->
                            {
                                marketParticipants.onConnected(connectionId);
                                marketConnectionApplication = new MarketConnectionApplication<>(clock, new ExchangeApplication(connectionId, clock, exchange, marketParticipants));
                                return marketConnectionApplication;
                            })
            );
        });
        application.onStart();
        long startTime = currentTime();
        while (!startedListening.get() && currentTime() < startTime + timeFromMs(timeoutMs))
        {
            application.work();
        }
        if (!startedListening.get())
        {
            throw new IllegalStateException("Unable to satisfy condition within " + timeoutMs + "ms.");
        }
        this.transportApplication = application;
        return application;
    }
}