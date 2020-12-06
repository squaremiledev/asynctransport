package dev.squaremile.transport.usecases.market;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
}