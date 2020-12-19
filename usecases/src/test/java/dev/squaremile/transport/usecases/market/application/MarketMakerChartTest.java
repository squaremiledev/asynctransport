package dev.squaremile.transport.usecases.market.application;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.transport.usecases.market.domain.TrackedSecurity;

class MarketMakerChartTest
{

    @Test
    void shouldLabelData()
    {
        assertThat(new MarketMakerChart().generateAsString()).isEqualTo("Time[s],Mid,Bid,Ask");
    }

    @Test
    void shouldPlotMidPrice()
    {
        MarketMakerChart chart = new MarketMakerChart();

        chart.onTick(new TrackedSecurity(10, 100, 9));
        chart.onTick(new TrackedSecurity(11, 101, 11));
        chart.onTick(new TrackedSecurity(13, 101, 11));

        assertThat(chart.generateAsString()).isEqualTo(
                "Time[s],Mid,Bid,Ask\n" +
                "0,100;100;100,0;0;0,0;0;0\n" +
                "1,101;101;101,0;0;0,0;0;0\n" +
                "3,101;101;101,0;0;0,0;0;0"
        );
    }

    @Test
    void shouldUseRelativeTime()
    {
        MarketMakerChart chart = new MarketMakerChart(TimeUnit.MILLISECONDS::toSeconds);

        chart.onTick(new TrackedSecurity(20_000, 100, 20_000));
        chart.onTick(new TrackedSecurity(30_000, 101, 30_000));
        chart.onTick(new TrackedSecurity(40_000, 103, 40_000));

        assertThat(chart.generateAsString()).isEqualTo(
                "Time[s],Mid,Bid,Ask\n" +
                "0,100;100;100,0;0;0,0;0;0\n" +
                "10,101;101;101,0;0;0,0;0;0\n" +
                "20,103;103;103,0;0;0,0;0;0"
        );
    }

    @Test
    void shouldSkipToFrequentUpdates()
    {
        MarketMakerChart chart = new MarketMakerChart(TimeUnit.MILLISECONDS::toSeconds);

        chart.onTick(new TrackedSecurity(20_000, 100, 20_000));
        chart.onTick(new TrackedSecurity(20_999, 101, 30_000));
        chart.onTick(new TrackedSecurity(30_100, 102, 30_100));
        chart.onTick(new TrackedSecurity(30_200, 103, 30_100));
        chart.onTick(new TrackedSecurity(30_999, 104, 30_999));
        chart.onTick(new TrackedSecurity(31_000, 105, 31_000));

        assertThat(chart.generateAsString()).isEqualTo(
                "Time[s],Mid,Bid,Ask\n" +
                "0,100;100;100,0;0;0,0;0;0\n" +
                "10,102;102;102,0;0;0,0;0;0\n" +
                "11,105;105;105,0;0;0,0;0;0"
        );
    }
}