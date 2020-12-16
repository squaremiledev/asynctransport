package dev.squaremile.transport.usecases.market.domain;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class FirmPriceTest
{
    private final Order executedOrderResult = new Order(0, 0, 0, 0);

    @Test
    void shouldUpdateAskQuantityWhenExecuted()
    {
        FirmPrice price = new FirmPrice(0, 19, 70, 21, 50);

        assertThat(price.execute(1, Order.bid(21, 10), executedOrderResult)).isTrue();

        assertThat(executedOrderResult).usingRecursiveComparison().isEqualTo(Order.bid(21, 10));
        assertThat(price).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 19, 70, 21, 40));
    }

    @Test
    void shouldUpdateBidQuantityWhenExecuted()
    {
        FirmPrice price = new FirmPrice(0, 19, 70, 21, 50);

        assertThat(price.execute(1, Order.ask(19, 10), executedOrderResult)).isTrue();

        assertThat(executedOrderResult).usingRecursiveComparison().isEqualTo(Order.ask(19, 10));
        assertThat(price).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 19, 60, 21, 50));
    }

    @Test
    void shouldUseBetterAskPriceIfAvailable()
    {
        FirmPrice price = new FirmPrice(0, 19, 70, 20, 50);

        assertThat(price.execute(1, Order.bid(21, 10), executedOrderResult)).isTrue();

        assertThat(executedOrderResult).usingRecursiveComparison().isEqualTo(Order.bid(20, 10));
        assertThat(price).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 19, 70, 20, 40));
    }

    @Test
    void shouldUseBetterBidPriceIfAvailable()
    {
        FirmPrice price = new FirmPrice(0, 20, 70, 21, 50);

        assertThat(price.execute(1, Order.ask(19, 10), executedOrderResult)).isTrue();

        assertThat(executedOrderResult).usingRecursiveComparison().isEqualTo(Order.ask(20, 10));
        assertThat(price).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 20, 60, 21, 50));
    }

    @Test
    void shouldAllowExecutingAllBidQuantity()
    {
        FirmPrice price = new FirmPrice(0, 19, 70, 21, 50);

        assertThat(price.execute(1, Order.bid(21, 50), executedOrderResult)).isTrue();

        assertThat(executedOrderResult).usingRecursiveComparison().isEqualTo(Order.bid(21, 50));
        assertThat(price).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 19, 70, 21, 0));
    }

    @Test
    void shouldAllowExecutingAllAskQuantity()
    {
        FirmPrice price = new FirmPrice(0, 19, 70, 21, 50);

        assertThat(price.execute(1, Order.ask(19, 70), executedOrderResult)).isTrue();

        assertThat(price).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 19, 0, 21, 50));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 51",
            "71, 0",
    })
    void shouldPreventFromExecutingBeyondProvidedLiquidity(final int executedAskQuantity, final int executedBidQuantity)
    {
        assertFailedToExecute(() -> new FirmPrice(0, 19, 70, 21, 50), () -> new Order(21, executedBidQuantity, 19, executedAskQuantity));
    }

    @Test
    void shouldPreventFromExecutingNonMatchingOrder()
    {
        assertFailedToExecute(() -> new FirmPrice(0, 19, 50, 21, 70), () -> new Order(0, 0, 0, 0));
        assertFailedToExecute(() -> new FirmPrice(0, 19, 50, 21, 70), () -> new Order(21, 0, 0, 0));
        assertFailedToExecute(() -> new FirmPrice(0, 19, 50, 21, 70), () -> new Order(21, 71, 0, 0));
        assertFailedToExecute(() -> new FirmPrice(0, 19, 50, 21, 70), () -> new Order(0, 0, 19, 0));
        assertFailedToExecute(() -> new FirmPrice(0, 19, 50, 21, 70), () -> new Order(0, 0, 19, 51));
    }

    private void assertFailedToExecute(final Supplier<FirmPrice> initialFirmPrice, final Supplier<Order> order)
    {
        FirmPrice price = initialFirmPrice.get();

        assertThat(price.execute(1, order.get(), executedOrderResult)).isFalse();

        assertThat(executedOrderResult).usingRecursiveComparison().isEqualTo(new Order(0, 0, 0, 0));
        assertThat(price).usingRecursiveComparison().isEqualTo(initialFirmPrice.get());
    }
}