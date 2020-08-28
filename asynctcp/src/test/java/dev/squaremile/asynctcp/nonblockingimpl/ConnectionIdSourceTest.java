package dev.squaremile.asynctcp.nonblockingimpl;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectionIdSourceTest
{
    @Test
    void shouldProvideNewIdEveryTimeAsked()
    {
        final ConnectionIdSource connectionIdSource = new ConnectionIdSource();
        final Set<Long> uniqueIds = new HashSet<>();
        for (int k = 0; k < 100; k++)
        {
            uniqueIds.add(connectionIdSource.newId());
        }
        assertEquals(100, uniqueIds.size());
    }
}