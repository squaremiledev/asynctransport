package dev.squaremile.transport.usecases.market.application;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.transport.usecases.market.domain.ExecutionReport;
import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.HeartBeat;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;
import dev.squaremile.transport.usecases.market.domain.Order;
import dev.squaremile.transport.usecases.market.domain.OrderResult;
import dev.squaremile.transport.usecases.market.domain.TrackedSecurity;

class SerializationTest
{

    private final Serialization serialization = new Serialization();
    private final MutableDirectBuffer buffer = new ExpandableArrayBuffer();

    @Test
    void shouldSerializeFirmPriceUpdate()
    {
        verifySerialization(new FirmPrice(5, 1234, 99, 40, 101, 50), 3);
    }

    @Test
    void shouldSerializeOrder()
    {
        verifySerialization(Order.bid(19, 50), 4);
        verifySerialization(Order.ask(21, 150), 9);
    }

    @Test
    void shouldSerializeOrderResult()
    {
        verifySerialization(OrderResult.NOT_EXECUTED, 5);
        verifySerialization(OrderResult.EXECUTED, 5);
    }

    @Test
    void shouldSerializeExecutionReport()
    {
        verifySerialization(new ExecutionReport().update(7, 14, new TrackedSecurity().update(100, 5_000, 99), Order.bid(5_001, 200)), 3);
        verifySerialization(new ExecutionReport().update(7, 14, new TrackedSecurity().update(100, 5_000, 99), Order.ask(5_001, 200)), 3);
    }

    @Test
    void shouldSerializeSecurity()
    {
        verifySerialization(new TrackedSecurity().update(100, 5_000, 99), 3);
    }

    @Test
    void shouldSerializeHeartBeat()
    {
        verifySerialization(new HeartBeat(), 4);
    }

    private void verifySerialization(final MarketMessage message, final int offset)
    {
        serialization.encode(message, buffer, offset);
        assertThat(serialization.decode(buffer, offset)).usingRecursiveComparison().isEqualTo(message);
    }
}