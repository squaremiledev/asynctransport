package com.michaelszymczak.sample.sockets.api;

import java.util.function.BooleanSupplier;

public interface Workman
{
    Runnable NOTHING_TO_RUN_BETWEEN_ITERATIONS = () ->
    {
    };

    void workUntil(final BooleanSupplier stopCondition, Runnable taskAfterIteration);

    void workUntil(final BooleanSupplier stopCondition);

    void work();
}
