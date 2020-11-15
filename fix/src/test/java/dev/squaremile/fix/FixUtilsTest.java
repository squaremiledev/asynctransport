package dev.squaremile.fix;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static dev.squaremile.fix.utils.FixUtils.asciiFixBody;

class FixUtilsTest
{
    @Test
    void shouldGenerateTheFixMessage()
    {
        // ignoring the checksum for now
        assertThat(
                new String(asciiFixBody("FIX.4.2", "35=5^49=SellSide^56=BuySide^34=3^52=20190606-09:25:34.329^58=Logout acknowledgement^")))
                .isEqualTo("8=FIX.4.2\u00019=84\u000135=5\u000149=SellSide\u000156=BuySide\u000134=3\u000152=20190606-09:25:34.329\u000158=Logout acknowledgement\u000110=079\u0001");
    }
}