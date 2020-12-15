package dev.squaremile.transport.usecases.market;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PnLTest
{
    private static int someMarketParticipant()
    {
        return 5;
    }

    @Test
    void shouldAssumeThereIsNoBalanceInitially()
    {
        assertThat(new PnL().estimatedNominalBalanceOf(someMarketParticipant())).isEqualTo(0);
    }

    @Test
    void shouldAssumeNoProfitIfExecutedPriceMatchesEstimatedMidPrice()
    {
        PnL pnl = new PnL();
        pnl.onExecution(6, 13, new TrackedSecurity().midPrice(100, 5_000), Order.ask(5_000, 100));
        pnl.onExecution(7, 14, new TrackedSecurity().midPrice(100, 5_000), Order.bid(5_000, 100));

        assertThat(pnl.estimatedNominalBalanceOf(6)).isEqualTo(0);
        assertThat(pnl.estimatedNominalBalanceOf(7)).isEqualTo(0);
        assertThat(pnl.estimatedNominalBalanceOf(13)).isEqualTo(0);
        assertThat(pnl.estimatedNominalBalanceOf(14)).isEqualTo(0);
    }

    @Test
    void shouldBalanceBeIndependentForEachMarketParticipan()
    {
        PnL pnl = new PnL();
        pnl.onExecution(6, 13, new TrackedSecurity().midPrice(100, 5_000), Order.ask(5_010, 5));

        assertThat(pnl.estimatedNominalBalanceOf(14)).isEqualTo(0);
    }

    @Test
    void shouldAssumeThatPassiveParticipantLosesOutWhenActivePartyExecutesBidWithPriceLowerThanEstimatedMidPrice()
    {
        PnL pnl = new PnL();
        pnl.onExecution(6, 13, new TrackedSecurity().midPrice(100, 5_000), Order.bid(4_990, 5));

        assertThat(pnl.estimatedNominalBalanceOf(6)).isEqualTo(-50);
        assertThat(pnl.estimatedNominalBalanceOf(13)).isEqualTo(50);
    }

    @Test
    void shouldAssumeThatPassiveParticipantProfitsWhenActivePartyExecutesAskWithPriceLowerThanEstimatedMidPrice()
    {
        PnL pnl = new PnL();
        pnl.onExecution(6, 13, new TrackedSecurity().midPrice(100, 4_000), Order.ask(3_980, 6));

        assertThat(pnl.estimatedNominalBalanceOf(6)).isEqualTo(120);
        assertThat(pnl.estimatedNominalBalanceOf(13)).isEqualTo(-120);
    }
}