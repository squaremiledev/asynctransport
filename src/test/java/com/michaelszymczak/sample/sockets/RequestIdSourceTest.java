package com.michaelszymczak.sample.sockets;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestIdSourceTest
{
    @Test
    void shouldProvideNewIdEveryTimeAsked()
    {
        final RequestIdSource requestIdSource = new RequestIdSource();
        final Set<Long> uniqueIds = new HashSet<>();
        for (int k = 0; k < 100; k++)
        {
            uniqueIds.add(requestIdSource.newId());
        }
        assertEquals(100, uniqueIds.size());
    }
}