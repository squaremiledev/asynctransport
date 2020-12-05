package dev.squaremile.transport.usecases.market;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChangeSensitiveSecurityTickerTest
{

    private final TickerSpy tickerSpy = new TickerSpy();

    @Test
    void shouldNotifyAboutSecurityTick()
    {
        ChangeSensitiveSecurityTicker ticker = new ChangeSensitiveSecurityTicker(10, tickerSpy);

        ticker.onTick(new TrackedSecurity(35, 100, 35));
        ticker.onTick(new TrackedSecurity(36, 101, 36));

        assertThat(tickerSpy.observedTicks()).hasSize(2);
        assertThat(tickerSpy.observedTick(0)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(35, 100, 35));
        assertThat(tickerSpy.observedTick(1)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(36, 101, 36));
    }

    @Test
    void shouldIgnoreIfNoPriceUpdates()
    {
        ChangeSensitiveSecurityTicker ticker = new ChangeSensitiveSecurityTicker(10, tickerSpy);

        ticker.onTick(new TrackedSecurity(35, 100, 35));
        ticker.onTick(new TrackedSecurity(35, 100, 35));
        ticker.onTick(new TrackedSecurity(36, 100, 35));

        assertThat(tickerSpy.observedTicks()).hasSize(1);
        assertThat(tickerSpy.observedTick(0)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(35, 100, 35));
    }

    @Test
    void shouldNotifyWhenPriceHasNotMovedForAWhile()
    {
        ChangeSensitiveSecurityTicker ticker = new ChangeSensitiveSecurityTicker(1000, tickerSpy);

        ticker.onTick(new TrackedSecurity(3500, 100, 3500));
        ticker.onTick(new TrackedSecurity(3600, 100, 3500));
        ticker.onTick(new TrackedSecurity(4500, 100, 3500));
        ticker.onTick(new TrackedSecurity(4600, 100, 3500));
        ticker.onTick(new TrackedSecurity(4700, 100, 3500));

        assertThat(tickerSpy.observedTicks()).hasSize(2);
        assertThat(tickerSpy.observedTick(0)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(3500, 100, 3500));
        assertThat(tickerSpy.observedTick(1)).usingRecursiveComparison().isEqualTo(new TrackedSecurity(4600, 100, 3500));
    }
}