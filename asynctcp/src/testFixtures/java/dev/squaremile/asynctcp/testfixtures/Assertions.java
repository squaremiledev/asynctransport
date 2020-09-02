package dev.squaremile.asynctcp.testfixtures;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;

import static org.assertj.core.api.Assertions.assertThat;


import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class Assertions
{
    @SafeVarargs
    public static <T> void assertEqual(final List<T> actual, final T... expected)
    {
        assertEqual(actual, asList(expected));
    }

    public static <T> void assertEqual(final List<T> actual, final List<T> expected)
    {
        assertEqual(actual, expected, emptyList());
    }

    public static <T> void assertEqual(final List<T> actual, final List<T> expected, final List<T> additionalExpected)
    {
        final ArrayList<T> totalExpected = new ArrayList<>();
        totalExpected.addAll(expected);
        totalExpected.addAll(additionalExpected);
        final RecursiveComparisonConfiguration recursiveComparisonConfiguration = new RecursiveComparisonConfiguration();
        recursiveComparisonConfiguration.strictTypeChecking(true);
        assertThat(new ArrayList<>(actual))
                .usingRecursiveComparison(recursiveComparisonConfiguration)
                .isEqualTo(new ArrayList<>(totalExpected));
    }
}
