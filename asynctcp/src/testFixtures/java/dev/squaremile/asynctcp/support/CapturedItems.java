package dev.squaremile.asynctcp.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;


import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class CapturedItems<T>
{
    private final List<T> items = new ArrayList<>();

    public void add(final T event)
    {
        items.add(event);
    }

    public List<T> all()
    {
        return new ArrayList<>(items);
    }

    public <Type extends T> Type last(final Class<Type> clazz, final Predicate<Type> predicate)
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

    public <Type extends T> List<Type> all(final Class<Type> itemType, final Predicate<Type> predicate)
    {
        return items.stream()
                .filter(itemType::isInstance)
                .map(itemType::cast)
                .filter(predicate)
                .collect(toList());
    }

    @Override
    public String toString()
    {
        return items.stream().map(Objects::toString).collect(joining("\n"));
    }
}