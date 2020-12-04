package dev.squaremile.asynctcp.transport.testfixtures;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;


import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class CapturedItems<T>
{
    private final List<T> items = new ArrayList<>();

    public synchronized void add(final T event)
    {
        items.add(event);
    }

    public synchronized List<T> all()
    {
        return new ArrayList<>(items);
    }

    public synchronized <Type extends T> Type last(final Class<Type> clazz, final Predicate<Type> predicate)
    {

        final List<Type> result = all(clazz, predicate);
        if (!result.isEmpty())
        {
            return result.get(result.size() - 1);
        }
        else
        {
            throw new IllegalStateException();
        }
    }

    public synchronized <Type extends T> List<Type> all(final Class<Type> itemType, final Predicate<Type> predicate)
    {
        return items.stream()
                .filter(itemType::isInstance)
                .map(itemType::cast)
                .filter(predicate)
                .collect(toList());
    }

    @Override
    public synchronized String toString()
    {
        return items.stream().map(Objects::toString).collect(joining("\n"));
    }
}