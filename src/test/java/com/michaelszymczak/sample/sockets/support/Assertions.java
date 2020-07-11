package com.michaelszymczak.sample.sockets.support;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;

import static org.assertj.core.api.Assertions.assertThat;


import static java.util.Arrays.asList;

public class Assertions
{
    @SafeVarargs
    public static <T> void assertEqual(final List<T> actual, final T... expected)
    {
        assertEqual(actual, asList(expected));
    }

    public static <T> void assertEqual(final List<T> actual, final List<T> expected)
    {
        final RecursiveComparisonConfiguration recursiveComparisonConfiguration = new RecursiveComparisonConfiguration();
        recursiveComparisonConfiguration.strictTypeChecking(true);
        assertThat(new ArrayList<>(actual))
                .usingRecursiveComparison(recursiveComparisonConfiguration)
                .isEqualTo(new ArrayList<>(expected));
    }
}
