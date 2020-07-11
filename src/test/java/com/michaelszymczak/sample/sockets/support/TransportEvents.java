package com.michaelszymczak.sample.sockets.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Predicate;

import com.michaelszymczak.sample.sockets.api.TransportEventsListener;
import com.michaelszymczak.sample.sockets.api.events.TransportEvent;

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

    public synchronized <T> T last(final Class<T> clazz)
    {
        final List<T> result = all(clazz);
        if (!result.isEmpty())
        {
            return result.get(result.size() - 1);
        }
        else
        {
            throw new IllegalStateException(clazz.getCanonicalName() + " not received");
        }
    }

    public synchronized <T> List<T> all(final Class<T> itemType)
    {
        return all(itemType, foundItem -> true);
    }

    public synchronized <T> List<T> all(final Class<T> itemType, final Predicate<T> predicate)
    {
        final ArrayList<T> result = new ArrayList<>();
        for (int j = 0; j < 10; j++)
        {
            for (int k = events.size() - 1; k >= 0; k--)
            {
                if (itemType.isInstance(events.get(k)))
                {
                    final T casted = itemType.cast(events.get(k));
                    if (predicate.test(casted))
                    {
                        result.add(casted);
                    }
                }
            }
            if (!result.isEmpty())
            {
                return result;
            }
            else
            {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
            }
        }
        return result;
    }
}
