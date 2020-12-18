package dev.squaremile.transport.usecases.market.application;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.transport.usecases.market.domain.ExecutionReport;
import dev.squaremile.transport.usecases.market.domain.MarketListener;
import dev.squaremile.transport.usecases.market.domain.Security;
import dev.squaremile.transport.usecases.market.domain.TickListener;

import static dev.squaremile.transport.usecases.market.application.ParticipantId.toConnectionId;

public class MarketEventsPublisher implements TickListener, MarketListener
{
    private final Transport transport;
    private final Serialization serialization = new Serialization();

    public MarketEventsPublisher(final Transport transport)
    {
        this.transport = transport;
    }

    @Override
    public void onExecution(final ExecutionReport executionReport)
    {
        send(executionReport, executionReport.passiveTraderId());
        send(executionReport, executionReport.aggressiveTraderId());
    }

    private void send(final ExecutionReport executionReport, final int marketParticipantId)
    {
        SendMessage sendMessage = transport.command(toConnectionId(marketParticipantId), SendMessage.class);
        MutableDirectBuffer buffer = sendMessage.prepare();
        int encodedLength = serialization.encode(executionReport, buffer, sendMessage.offset());
        sendMessage.commit(encodedLength);
        transport.handle(sendMessage);
    }

    @Override
    public void onTick(final Security security)
    {

    }
}
