package dev.squaremile.transport.usecases.market.application;

import java.util.function.LongConsumer;

import org.agrona.MutableDirectBuffer;


import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.SendMessage;
import dev.squaremile.transport.usecases.market.domain.ExecutionReport;
import dev.squaremile.transport.usecases.market.domain.MarketListener;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;
import dev.squaremile.transport.usecases.market.domain.Security;
import dev.squaremile.transport.usecases.market.domain.TickListener;

public class MarketEventsPublisher implements TickListener, MarketListener
{
    private final MarketParticipants marketParticipants;
    private final MessageToSend messageToSend;

    public MarketEventsPublisher(final Transport transport, final MarketParticipants marketParticipants)
    {
        this.marketParticipants = marketParticipants;
        this.messageToSend = new MessageToSend(transport);
    }

    @Override
    public void onExecution(final ExecutionReport executionReport)
    {
        messageToSend.with(executionReport).accept(executionReport.passiveTraderId());
        messageToSend.with(executionReport).accept(executionReport.aggressiveTraderId());
    }

    @Override
    public void onTick(final Security security)
    {
        marketParticipants.forEachConnectedParticipantConnectionId(messageToSend.with(security));
    }

    private static class MessageToSend implements LongConsumer
    {
        private final Transport transport;
        private final Serialization serialization = new Serialization();
        private MarketMessage marketMessage;

        private MessageToSend(final Transport transport)
        {
            this.transport = transport;
        }

        MessageToSend with(final MarketMessage marketMessage)
        {
            this.marketMessage = marketMessage;
            return this;
        }

        @Override
        public void accept(final long connectionId)
        {
            send(marketMessage, connectionId);
        }

        private void send(final MarketMessage marketMessage, final long connectionId)
        {
            SendMessage sendMessage = transport.command(connectionId, SendMessage.class);
            MutableDirectBuffer buffer = sendMessage.prepare();
            int encodedLength = serialization.encode(marketMessage, buffer, sendMessage.offset());
            sendMessage.commit(encodedLength);
            transport.handle(sendMessage);
        }
    }
}
