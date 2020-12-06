package dev.squaremile.transport.usecases.market;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class FirmPriceTest
{
    @Test
    void shouldUpdateAskQuantityWhenExecuted()
    {
        FirmPrice price = new FirmPrice(0, 21, 50, 19, 70);

        assertThat(price.execute(1, new FirmPrice(1, 21, 10, 19, 0))).isTrue();

        assertThat(price).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 21, 40, 19, 70));
    }

    @Test
    void shouldUpdateBidQuantityWhenExecuted()
    {
        FirmPrice price = new FirmPrice(0, 21, 50, 19, 70);

        assertThat(price.execute(1, new FirmPrice(1, 21, 0, 19, 10))).isTrue();

        assertThat(price).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 21, 50, 19, 60));
    }

    @Test
    void shouldAllowExecutingAllAskQuantity()
    {
        FirmPrice price = new FirmPrice(0, 21, 50, 19, 70);

        assertThat(price.execute(1, new FirmPrice(1, 21, 50, 19, 0))).isTrue();

        assertThat(price).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 21, 0, 19, 70));
    }

    @Test
    void shouldAllowExecutingAllBidQuantity()
    {
        FirmPrice price = new FirmPrice(0, 21, 50, 19, 70);

        assertThat(price.execute(1, new FirmPrice(1, 21, 0, 19, 70))).isTrue();

        assertThat(price).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 21, 50, 19, 0));
    }

    @ParameterizedTest
    @CsvSource({
            "51, 0",
            "0, 71",
    })
    void shouldPreventFromExecutingBeyondProvidedLiquidity(final int executedAskQuantity, final int executedBidQuantity)
    {
        assertFailedToExecute(() -> new FirmPrice(0, 21, 50, 19, 70), () -> new FirmPrice(0, 21, executedAskQuantity, 19, executedBidQuantity));
    }

    @Test
    void shouldPreventFromExecutingNegativeQuantity()
    {
        assertFailedToExecute(() -> new FirmPrice(0, 21, 50, 19, 70), () -> new FirmPrice(0, 21, -1, 19, 0));
        assertFailedToExecute(() -> new FirmPrice(0, 21, 50, 19, 70), () -> new FirmPrice(0, 21, 0, 19, -1));
    }

    @Test
    void shouldPreventFromExecutingWrongPrice()
    {
        assertFailedToExecute(() -> new FirmPrice(0, 21, 50, 19, 70), () -> new FirmPrice(0, 22, 10, 19, 0));
        assertFailedToExecute(() -> new FirmPrice(0, 21, 50, 19, 70), () -> new FirmPrice(0, 19, 10, 19, 0));
        assertFailedToExecute(() -> new FirmPrice(0, 21, 50, 19, 70), () -> new FirmPrice(0, 21, 10, 20, 0));
        assertFailedToExecute(() -> new FirmPrice(0, 21, 50, 19, 70), () -> new FirmPrice(0, 21, 10, 21, 0));
        assertFailedToExecute(() -> new FirmPrice(0, 21, 50, 19, 70), () -> new FirmPrice(0, 22, 0, 19, 10));
        assertFailedToExecute(() -> new FirmPrice(0, 21, 50, 19, 70), () -> new FirmPrice(0, 19, 0, 19, 10));
        assertFailedToExecute(() -> new FirmPrice(0, 21, 50, 19, 70), () -> new FirmPrice(0, 21, 0, 20, 10));
        assertFailedToExecute(() -> new FirmPrice(0, 21, 50, 19, 70), () -> new FirmPrice(0, 21, 0, 21, 10));
    }


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
        assertFailedToExecute(() -> new FirmPrice(0, 21, 50, 19, 70), () -> new FirmPrice(0, 21, executedAskQuantity, 19, executedBidQuantity));
    }

    private void assertFailedToExecute(final Supplier<FirmPrice> initialFirmPrice, final Supplier<FirmPrice> executedQuantity)
    {
        FirmPrice price = initialFirmPrice.get();

        assertThat(price.execute(1, executedQuantity.get())).isFalse();

        assertThat(price).usingRecursiveComparison().isEqualTo(initialFirmPrice.get());
    }
}