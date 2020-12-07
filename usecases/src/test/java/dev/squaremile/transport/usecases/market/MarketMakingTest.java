package dev.squaremile.transport.usecases.market;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarketMakingTest
{
    private final MarketMaking marketMaking = new MarketMaking();

    private static int marketMaker(final int id)
    {
        return id;
    }

    @Test
    void shouldFailToExecuteIfNotEnoughMarketDepth()
    {
        assertThat(marketMaking.execute(1, Order.bid(21, 50))).isFalse();
    }

    @Test
    void shouldExecuteQuantity()
    {
        marketMaking.updateFirmPrice(0, marketMaker(1), new FirmPrice(0, 19, 60, 21, 50));

        assertThat(marketMaking.execute(1, Order.bid(21, 40))).isTrue();

        assertThat(marketMaking.firmPrice(marketMaker(1))).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 19, 60, 21, 10));
    }

    @Test
    void shouldExecuteBestAskPrice()
    {
        marketMaking.updateFirmPrice(1, marketMaker(1), new FirmPrice(0, 18, 90, 21, 40));
        marketMaking.updateFirmPrice(2, marketMaker(2), new FirmPrice(0, 17, 90, 18, 50));
        marketMaking.updateFirmPrice(3, marketMaker(3), new FirmPrice(0, 18, 90, 20, 60));
        marketMaking.updateFirmPrice(6, marketMaker(6), new FirmPrice(0, 18, 90, 19, 100));
        marketMaking.updateFirmPrice(4, marketMaker(4), new FirmPrice(0, 18, 90, 19, 60));
        marketMaking.updateFirmPrice(5, marketMaker(5), new FirmPrice(0, 18, 90, 19, 90));

        assertThat(marketMaking.execute(7, Order.bid(20, 55))).isTrue();

        assertThat(marketMaking.firmPrice(marketMaker(1))).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 18, 90, 21, 40));
        assertThat(marketMaking.firmPrice(marketMaker(2))).usingRecursiveComparison().isEqualTo(new FirmPrice(2, 17, 90, 18, 50));
        assertThat(marketMaking.firmPrice(marketMaker(3))).usingRecursiveComparison().isEqualTo(new FirmPrice(3, 18, 90, 20, 60));
        assertThat(marketMaking.firmPrice(marketMaker(6))).usingRecursiveComparison().isEqualTo(new FirmPrice(6, 18, 90, 19, 100));
        assertThat(marketMaking.firmPrice(marketMaker(4))).usingRecursiveComparison().isEqualTo(new FirmPrice(7, 18, 90, 19, 5));
        assertThat(marketMaking.firmPrice(marketMaker(5))).usingRecursiveComparison().isEqualTo(new FirmPrice(5, 18, 90, 19, 90));
    }

    @Test
    void shouldExecuteBestBidPrice()
    {
        marketMaking.updateFirmPrice(1, marketMaker(1), new FirmPrice(0, 19, 40, 18, 90));
        marketMaking.updateFirmPrice(2, marketMaker(2), new FirmPrice(0, 22, 50, 17, 90));
        marketMaking.updateFirmPrice(3, marketMaker(3), new FirmPrice(0, 20, 60, 18, 90));
        marketMaking.updateFirmPrice(6, marketMaker(6), new FirmPrice(0, 21, 100, 18, 90));
        marketMaking.updateFirmPrice(4, marketMaker(4), new FirmPrice(0, 21, 60, 18, 90));
        marketMaking.updateFirmPrice(5, marketMaker(5), new FirmPrice(0, 21, 90, 18, 90));

        assertThat(marketMaking.execute(7, Order.ask(20, 55))).isTrue();

        assertThat(marketMaking.firmPrice(marketMaker(1))).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 19, 40, 18, 90));
        assertThat(marketMaking.firmPrice(marketMaker(2))).usingRecursiveComparison().isEqualTo(new FirmPrice(2, 22, 50, 17, 90));
        assertThat(marketMaking.firmPrice(marketMaker(3))).usingRecursiveComparison().isEqualTo(new FirmPrice(3, 20, 60, 18, 90));
        assertThat(marketMaking.firmPrice(marketMaker(6))).usingRecursiveComparison().isEqualTo(new FirmPrice(6, 21, 100, 18, 90));
        assertThat(marketMaking.firmPrice(marketMaker(4))).usingRecursiveComparison().isEqualTo(new FirmPrice(7, 21, 5, 18, 90));
        assertThat(marketMaking.firmPrice(marketMaker(5))).usingRecursiveComparison().isEqualTo(new FirmPrice(5, 21, 90, 18, 90));
    }
}