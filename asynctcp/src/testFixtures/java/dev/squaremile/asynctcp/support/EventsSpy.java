package dev.squaremile.asynctcp.support;

import java.util.List;
import java.util.function.Predicate;

public abstract class EventsSpy<E>
{
    private final CapturedItems<E> items;

    EventsSpy(final CapturedItems<E> items)
    {
        this.items = items;
    }

    public final <T extends E> boolean contains(final Class<T> itemType)
    {
        return contains(itemType, all -> true);
    }

    public final <T extends E> boolean contains(final Class<T> itemType, final Predicate<T> predicate)
    {
        return !all(itemType, predicate).isEmpty();
    }

    public final List<E> all()
    {
        return items.all();
    }

    public final <T extends E> List<T> all(final Class<T> itemType)
    {
        return items.all(itemType, foundItem -> true);
    }

    public final <T extends E> List<T> all(final Class<T> itemType, final Predicate<T> predicate)
    {
        return items.all(itemType, predicate);
    }

    public final <T extends E> T last(final Class<T> clazz)
    {
        return items.last(clazz, event -> true);
    }

    public final <T extends E> T last(final Class<T> clazz, final Predicate<T> predicate)
    {
        return items.last(clazz, predicate);
    }

    @Override
    public final String toString()
    {
        return items.toString();
    }
}
