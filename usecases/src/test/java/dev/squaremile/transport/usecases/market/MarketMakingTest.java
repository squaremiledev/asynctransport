package dev.squaremile.transport.usecases.market;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarketMakingTest
{
    private static final int MARKET_MAKER_A = 1;
    private static final int MARKET_MAKER_B = 2;
    private static final int MARKET_MAKER_C = 3;
    private static final int MARKET_MAKER_D = 4;

    private final MarketMaking marketMaking = new MarketMaking();

    @Test
    void shouldFailToExecuteIfNotEnoughMarketDepth()
    {
        assertThat(marketMaking.execute(1, new FirmPrice(1, 21, 50, 19, 60))).isFalse();
    }

    @Test
    void shouldExecuteQuantity()
    {
        marketMaking.updateFirmPrice(0, MARKET_MAKER_A, new FirmPrice(0, 21, 50, 19, 60));

        assertThat(marketMaking.execute(1, new FirmPrice(1, 21, 40, 19, 0))).isTrue();

        assertThat(marketMaking.firmPrice(MARKET_MAKER_A)).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 21, 10, 19, 60));
    }

    @Test
    @Disabled
    void shouldExecuteBestPriceWithSufficientQuantity()
    {
        marketMaking.updateFirmPrice(1, MARKET_MAKER_A, new FirmPrice(1, 21, 40, 18, 60));
        marketMaking.updateFirmPrice(2, MARKET_MAKER_B, new FirmPrice(2, 19, 50, 18, 60));
        marketMaking.updateFirmPrice(3, MARKET_MAKER_C, new FirmPrice(3, 20, 60, 18, 60));
        marketMaking.updateFirmPrice(4, MARKET_MAKER_D, new FirmPrice(4, 20, 100, 18, 60));

        assertThat(marketMaking.execute(5, new FirmPrice(5, 21, 55, 18, 0))).isTrue();

        assertThat(marketMaking.firmPrice(MARKET_MAKER_A)).usingRecursiveComparison().isEqualTo(new FirmPrice(0, 21, 40, 18, 60));
        assertThat(marketMaking.firmPrice(MARKET_MAKER_B)).usingRecursiveComparison().isEqualTo(new FirmPrice(0, 19, 50, 18, 60));
        assertThat(marketMaking.firmPrice(MARKET_MAKER_C)).usingRecursiveComparison().isEqualTo(new FirmPrice(1, 20, 5, 18, 60));
        assertThat(marketMaking.firmPrice(MARKET_MAKER_D)).usingRecursiveComparison().isEqualTo(new FirmPrice(0, 20, 100, 18, 60));
    }
}