package dev.squaremile.asynctcpacceptance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static java.util.stream.LongStream.rangeClosed;

class SelectiveResponseRequestTest
{
    @Test
    void shouldValidateData()
    {
        assertThrows(IllegalArgumentException.class, () -> new SelectiveResponseRequest(-1));
        assertThrows(IllegalArgumentException.class, () -> new SelectiveResponseRequest(0));
        assertThrows(IllegalArgumentException.class, () -> new SelectiveResponseRequest(1).shouldRespond(-1));
    }

    @Test
    void shouldAlwaysRespondToTheFirstMessage()
    {
        assertThat(new SelectiveResponseRequest(1).shouldRespond(0)).isTrue();
        assertThat(new SelectiveResponseRequest(3).shouldRespond(0)).isTrue();
    }

    @Test
    void shouldBeAbleToTellToRespondToEveryMessage()
    {
        SelectiveResponseRequest selectiveResponseRequest = new SelectiveResponseRequest(1);
        assertThat(selectiveResponseRequest.shouldRespond(1)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(2)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(3)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(1000)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(1001)).isTrue();
        assertThat(rangeClosed(0, 10_000).filter(selectiveResponseRequest::shouldRespond).count()).isEqualTo(10_000 + 1);
    }

    @Test
    void shouldTellNotToRespondToEveryNthMessage()
    {
        SelectiveResponseRequest selectiveResponseRequest = new SelectiveResponseRequest(3);
        assertThat(selectiveResponseRequest.shouldRespond(0)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(1)).isFalse();
        assertThat(selectiveResponseRequest.shouldRespond(2)).isFalse();
        assertThat(selectiveResponseRequest.shouldRespond(3)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(999)).isTrue();
        assertThat(selectiveResponseRequest.shouldRespond(1000)).isFalse();
        assertThat(selectiveResponseRequest.shouldRespond(1001)).isFalse();
        assertThat(selectiveResponseRequest.shouldRespond(1002)).isTrue();
        assertThat(rangeClosed(0, 10_000).filter(selectiveResponseRequest::shouldRespond).count()).isEqualTo((10_000 / 3) + 1);
    }
}