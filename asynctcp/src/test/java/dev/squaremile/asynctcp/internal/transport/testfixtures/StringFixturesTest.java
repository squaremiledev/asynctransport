package dev.squaremile.asynctcp.internal.transport.testfixtures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


import static dev.squaremile.asynctcp.fixtures.transport.StringFixtures.byteArrayWith;
import static dev.squaremile.asynctcp.fixtures.transport.StringFixtures.stringWith;

class StringFixturesTest
{
    @Test
    void shouldGenerateHumanReadableData()
    {
        assertEquals("0", stringWith(byteArrayWith(pos -> "0", 1)));
        assertEquals("4", stringWith(byteArrayWith(pos -> "4", 1)));
        assertEquals("0", stringWith(byteArrayWith(pos -> "" + pos, 1)));
    }

    @Test
    void shouldLimitTheNumberOfGeneratedItems()
    {
        assertEquals("000", stringWith(byteArrayWith(pos -> "0", 3)));
        assertEquals("yy", stringWith(byteArrayWith(pos -> "y", 2)));
        assertEquals("01", stringWith(byteArrayWith(pos -> "" + pos, 2)));
        assertEquals("   0   1", stringWith(byteArrayWith(pos -> "   " + pos, 2)));
        assertEquals(" 0\n 1\n 2\n", stringWith(byteArrayWith(pos -> String.format("%2d%n", pos), 3)));
    }
}