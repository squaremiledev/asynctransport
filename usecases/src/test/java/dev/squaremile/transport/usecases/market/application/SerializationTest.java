package dev.squaremile.transport.usecases.market.application;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;

class SerializationTest
{
    @Test
    void shouldSerializeFirmPriceUpdate()
    {
        Serialization serialization = new Serialization();
        MutableDirectBuffer buffer = new ExpandableArrayBuffer();
        FirmPrice firmPrice = new FirmPrice(5, 1234, 99, 40, 101, 50);
        serialization.encode(firmPrice, buffer, 3);
        MarketMessage decoded = serialization.decode(buffer, 3);
        Assertions.assertThat(decoded).usingRecursiveComparison().isEqualTo(firmPrice);
    }
}