package com.michaelszymczak.sample.sockets.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.michaelszymczak.sample.sockets.api.TransportEventsListener;
import com.michaelszymczak.sample.sockets.api.events.TransportEvent;


import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class TransportEvents implements TransportEventsListener
{
    private final List<TransportEvent> events = new ArrayList<>();

    @Override
    public synchronized void onEvent(final TransportEvent event)
    {
        events.add(event);
    }

    public synchronized List<TransportEvent> events()
    {
        return new ArrayList<>(events);
    }

    public <T extends TransportEvent> T last(final Class<T> clazz)
    {
        return last(clazz, event -> true);
    }

    public <T extends TransportEvent> T last(final Class<T> clazz, final Predicate<T> predicate)
    {

        final List<T> result = all(clazz, predicate);
        if (!result.isEmpty())
        {
            return result.get(result.size() - 1);
        }
        else
        {
            throw new IllegalStateException();
        }
    }

    public <T extends TransportEvent> List<T> all(final Class<T> itemType)
    {
        return all(itemType, foundItem -> true);
    }

    public <T extends TransportEvent> List<T> all(final Class<T> itemType, final Predicate<T> predicate)
    {
        return events.stream()
                .filter(itemType::isInstance)
                .map(itemType::cast)
                .filter(predicate)
                .collect(toList());
    }

    @Override
    public String toString()
    {
        return events.stream().map(Objects::toString).collect(joining("\n"));
    }
}