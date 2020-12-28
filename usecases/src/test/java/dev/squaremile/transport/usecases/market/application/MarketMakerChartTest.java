package dev.squaremile.transport.usecases.market.application;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.TrackedSecurity;

import static java.nio.charset.StandardCharsets.US_ASCII;

class MarketMakerChartTest
{
    @Test
    void shouldLabelData()
    {
        assertThat(new MarketMakerChart().generateAsString()).isEqualTo("Time[s],Mid,Bid,Ask\n");
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
                "3,101;101;101,0;0;0,0;0;0\n"
        );
    }

    @Test
    void shouldUseRelativeTime()
    {
        MarketMakerChart chart = new MarketMakerChart(TimeUnit.MILLISECONDS::toSeconds, 0);

        chart.onTick(new TrackedSecurity(20_000, 100, 20_000));
        chart.onTick(new TrackedSecurity(30_000, 101, 30_000));
        chart.onTick(new TrackedSecurity(40_000, 103, 40_000));

        assertThat(chart.generateAsString()).isEqualTo(
                "Time[s],Mid,Bid,Ask\n" +
                "0,100;100;100,0;0;0,0;0;0\n" +
                "10,101;101;101,0;0;0,0;0;0\n" +
                "20,103;103;103,0;0;0,0;0;0\n"
        );
    }


    @Test
    void shouldUseRelativeTimeOfFirmPrice()
    {
        MarketMakerChart chart = new MarketMakerChart(TimeUnit.MILLISECONDS::toSeconds, 0);

        chart.onFirmPriceUpdate(marketMakerId(), FirmPrice.spreadFirmPrice(1000, 50, 100, 4));
        chart.onFirmPriceUpdate(marketMakerId(), FirmPrice.spreadFirmPrice(2000, 50, 100, 5));
        chart.onFirmPriceUpdate(marketMakerId(), FirmPrice.spreadFirmPrice(5000, 50, 100, 3));


        assertThat(chart.generateAsString()).isEqualTo(
                "Time[s],Mid,Bid,Ask\n" +
                "0,0;0;0,96;96;96,104;104;104\n" +
                "1,0;0;0,95;95;95,105;105;105\n" +
                "4,0;0;0,97;97;97,103;103;103\n"
        );
    }

    @Test
    void shouldSkipToFrequentUpdates()
    {
        MarketMakerChart chart = new MarketMakerChart(TimeUnit.MILLISECONDS::toSeconds, 0);

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
                "11,105;105;105,0;0;0,0;0;0\n"
        );
    }

    @Test
    void shouldPlotFirmPrices()
    {
        MarketMakerChart chart = new MarketMakerChart();

        chart.onFirmPriceUpdate(marketMakerId(), FirmPrice.spreadFirmPrice(0, 50, 100, 4));

        assertThat(chart.generateAsString()).isEqualTo(
                "Time[s],Mid,Bid,Ask\n" +
                "0,0;0;0,96;96;96,104;104;104\n"
        );
    }

    @Test
    void shouldIgnoreTooFrequentUpdatesOfFirmPrices()
    {
        MarketMakerChart chart = new MarketMakerChart(TimeUnit.MILLISECONDS::toSeconds, 0);
        chart.onFirmPriceUpdate(marketMakerId(), FirmPrice.spreadFirmPrice(1000, 50, 100, 4));

        chart.onFirmPriceUpdate(marketMakerId(), FirmPrice.spreadFirmPrice(1999, 50, 100, 4));
        chart.onFirmPriceUpdate(marketMakerId(), FirmPrice.spreadFirmPrice(2000, 50, 100, 4));

        assertThat(chart.generateAsString()).isEqualTo(
                "Time[s],Mid,Bid,Ask\n" +
                "0,0;0;0,96;96;96,104;104;104\n" +
                "1,0;0;0,96;96;96,104;104;104\n"
        );
    }

    @Test
    void shouldRepeatTheLastMidPrice()
    {
        MarketMakerChart chart = new MarketMakerChart();
        chart.onTick(new TrackedSecurity(0, 95, 0));

        chart.onFirmPriceUpdate(marketMakerId(), FirmPrice.spreadFirmPrice(1, 50, 100, 4));

        assertThat(chart.generateAsString()).isEqualTo(
                "Time[s],Mid,Bid,Ask\n" +
                "0,95;95;95,0;0;0,0;0;0\n" +
                "1,95;95;95,96;96;96,104;104;104\n"
        );
    }

    @Test
    void shouldRepeatTheLastFirmPrice()
    {
        MarketMakerChart chart = new MarketMakerChart();
        chart.onFirmPriceUpdate(marketMakerId(), FirmPrice.spreadFirmPrice(0, 50, 100, 4));

        chart.onTick(new TrackedSecurity(1, 95, 0));

        assertThat(chart.generateAsString()).isEqualTo(
                "Time[s],Mid,Bid,Ask\n" +
                "0,0;0;0,96;96;96,104;104;104\n" +
                "1,95;95;95,96;96;96,104;104;104\n"
        );
    }

    @Test
    void shouldGenerateStringBytes()
    {
        MarketMakerChart chart = new MarketMakerChart();
        String expectedContent = chart.generateAsString();
        assertThat(expectedContent).isNotEmpty();

        assertThat(chart.generateAsStringBytes()).isEqualTo(expectedContent.getBytes(US_ASCII));

    }

    @Test
    @Disabled
    void shouldIncludeEntriesIfTheConvertedTimeRemainsTheSameButTheValuesChanged()
    {

    }

    private int marketMakerId()
    {
        return 7;
    }
}