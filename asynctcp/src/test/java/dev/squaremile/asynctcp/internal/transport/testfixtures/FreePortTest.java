package dev.squaremile.asynctcp.internal.transport.testfixtures;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.fixtures.transport.FreePort;

class FreePortTest
{
    @Test
    void shouldProvideLabeledFreePortPools()
    {
        Map<String, List<Integer>> pools = FreePort.freePortPools("foo:1", "bar:3", "baz:0");
        assertThat(pools).hasSize(3);
        assertThat(pools).containsKeys("foo", "bar", "baz");
        assertThat(pools.get("foo")).hasSize(1);
        assertThat(pools.get("bar")).hasSize(3);
        assertThat(pools.get("baz")).hasSize(0);
    }
}