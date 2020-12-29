package dev.squaremile.transport.casestudy.marketmaking.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest
{
    @ParameterizedTest
    @CsvSource({
            "10, 15",
            "50, 15",
            "15, 70",
            "51, 70",
            "50, 71",
            "51, 71",
    })
    void shouldPreventFromExecutingBothSides(final int executedAskQuantity, final int executedBidQuantity)
    {
        assertThrows(IllegalArgumentException.class, () -> new Order(21, executedAskQuantity, 19, executedBidQuantity));
    }

    @Test
    void shouldTellTheSide()
    {
        assertThat(new Order(9, 150, 0, 0).side()).isEqualTo(Side.BID);
        assertThat(new Order(0, 0, 9, 150).side()).isEqualTo(Side.ASK);
        assertThat(Order.bid(9, 150).side()).isEqualTo(Side.BID);
        assertThat(Order.ask(9, 150).side()).isEqualTo(Side.ASK);
    }
}