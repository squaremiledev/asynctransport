package com.michaelszymczak.sample.sockets.support;

import java.util.List;
import java.util.function.Predicate;

public class SynchronizedCapturedItems<T> extends CapturedItems<T>
{
    public synchronized void add(final T event)
    {
        System.out.println(event);
        super.add(event);
    }

    public synchronized List<T> all()
    {
        return super.all();
    }

    public synchronized <Type extends T> Type last(final Class<Type> clazz, final Predicate<Type> predicate)
    {
        return super.last(clazz, predicate);
    }

    public synchronized <Type extends T> List<Type> all(final Class<Type> itemType, final Predicate<Type> predicate)
    {
        return super.all(itemType, predicate);
    }

    @Override
    public synchronized String toString()
    {
        return super.toString();
    }
}