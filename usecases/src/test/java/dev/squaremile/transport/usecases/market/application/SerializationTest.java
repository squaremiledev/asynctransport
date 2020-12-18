package dev.squaremile.transport.usecases.market.application;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;
import dev.squaremile.transport.usecases.market.domain.Order;
import dev.squaremile.transport.usecases.market.domain.OrderResult;

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
    }

    private void verifySerialization(final MarketMessage message, final int offset)
    {
        serialization.encode(message, buffer, offset);
        assertThat(serialization.decode(buffer, offset)).usingRecursiveComparison().isEqualTo(message);
    }
}