package com.michaelszymczak.sample.sockets;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionIdSourceTest
{
    @Test
    void shouldProvideNewIdEveryTimeAsked()
    {
        final SessionIdSource sessionIdSource = new SessionIdSource();
        final Set<Long> uniqueIds = new HashSet<>();
        for (int k = 0; k < 100; k++)
        {
            uniqueIds.add(sessionIdSource.newId());
        }
        assertEquals(100, uniqueIds.size());
    }
}