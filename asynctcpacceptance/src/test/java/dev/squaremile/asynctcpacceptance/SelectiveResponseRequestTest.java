package dev.squaremile.asynctcpacceptance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static java.util.stream.LongStream.rangeClosed;

class SelectiveResponseRequestTest
{

    public static final int TOTAL = 10_000;

    @Test
    void shouldValidateData()
    {
        assertThrows(IllegalArgumentException.class, () -> new SelectiveResponseRequest(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> new SelectiveResponseRequest(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new SelectiveResponseRequest(TOTAL, 1).receivedLast(-1));
        assertThrows(IllegalArgumentException.class, () -> new SelectiveResponseRequest(TOTAL, 1).receivedLast(0));
        assertThrows(IllegalArgumentException.class, () -> new SelectiveResponseRequest(TOTAL, -1));
        assertThrows(IllegalArgumentException.class, () -> new SelectiveResponseRequest(TOTAL, 0));
        assertThrows(IllegalArgumentException.class, () -> new SelectiveResponseRequest(TOTAL, 1).shouldRespond(-1));
    }

    @Test
    void shouldRejectIfTriesToSendNotEvenlyDivisibleByResponseRate()
    {
        assertThrows(IllegalArgumentException.class, () -> new SelectiveResponseRequest(5, 3));
    }

    @Test
    void shouldAlwaysRespondToTheFirstMessage()
    {
        assertThat(new SelectiveResponseRequest(10, 1).shouldRespond(0)).isTrue();
        assertThat(new SelectiveResponseRequest(9, 3).shouldRespond(0)).isTrue();
    }

    @Test
    void shouldNeverRespondIfAllMessagesAlreadySent()
    {
        assertThat(new SelectiveResponseRequest(10, 1).shouldRespond(10)).isFalse();
        assertThat(new SelectiveResponseRequest(9, 3).shouldRespond(9)).isFalse();
    }

    @Test
    void shouldBeAbleToTellToRespondToEveryMessage()
    {
        SelectiveResponseRequest selectiveResponseRequest = new SelectiveResponseRequest(TOTAL, 1);
        assertThat(selectiveResponseRequest.shouldRespond(1)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(2)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(3)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(1000)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(1001)).isTrue();
        assertThat(rangeClosed(0, TOTAL).filter(selectiveResponseRequest::shouldRespond).count()).isEqualTo(TOTAL);
    }

    @Test
    void shouldTellNotToRespondToEveryNthMessage()
    {
        SelectiveResponseRequest selectiveResponseRequest = new SelectiveResponseRequest(3000, 3);
        assertThat(selectiveResponseRequest.shouldRespond(0)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(1)).isFalse();
        assertThat(selectiveResponseRequest.shouldRespond(2)).isFalse();
        assertThat(selectiveResponseRequest.shouldRespond(3)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(999)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(1000)).isFalse();
        assertThat(selectiveResponseRequest.shouldRespond(1001)).isFalse();
        assertThat(selectiveResponseRequest.shouldRespond(1002)).isTrue();
        assertThat(rangeClosed(0, 3000).filter(selectiveResponseRequest::shouldRespond).count()).isEqualTo(1000);
    }

    @Test
    void shouldTellThatReceivedLastIfAlreadyHappened()
    {
        assertThat(new SelectiveResponseRequest(3, 1).receivedLast(100)).isTrue();
    }

    @Test
    void shouldTellIfReceivedLastResponseForGivenRatio()
    {
        assertThat(new SelectiveResponseRequest(3, 1).shouldRespond(0)).isTrue();
        assertThat(new SelectiveResponseRequest(3, 1).receivedLast(1)).isFalse();
        assertThat(new SelectiveResponseRequest(3, 1).shouldRespond(1)).isTrue();
        assertThat(new SelectiveResponseRequest(3, 1).receivedLast(2)).isFalse();
        assertThat(new SelectiveResponseRequest(3, 1).shouldRespond(2)).isTrue();
        assertThat(new SelectiveResponseRequest(3, 1).receivedLast(3)).isTrue();
        assertThat(new SelectiveResponseRequest(3, 1).shouldRespond(3)).isFalse();

        assertThat(new SelectiveResponseRequest(9, 3).shouldRespond(0)).isTrue();
        assertThat(new SelectiveResponseRequest(9, 3).receivedLast(1)).isFalse();
        assertThat(new SelectiveResponseRequest(9, 3).shouldRespond(1)).isFalse();
        assertThat(new SelectiveResponseRequest(9, 3).shouldRespond(2)).isFalse();
        assertThat(new SelectiveResponseRequest(9, 3).shouldRespond(3)).isTrue();
        assertThat(new SelectiveResponseRequest(9, 3).receivedLast(2)).isFalse();
        assertThat(new SelectiveResponseRequest(9, 3).shouldRespond(4)).isFalse();
        assertThat(new SelectiveResponseRequest(9, 3).shouldRespond(5)).isFalse();
        assertThat(new SelectiveResponseRequest(9, 3).shouldRespond(6)).isTrue();
        assertThat(new SelectiveResponseRequest(9, 3).receivedLast(3)).isTrue();
        assertThat(new SelectiveResponseRequest(9, 3).shouldRespond(7)).isFalse();
        assertThat(new SelectiveResponseRequest(9, 3).shouldRespond(8)).isFalse();
        assertThat(new SelectiveResponseRequest(9, 3).shouldRespond(9)).isFalse();
    }
}