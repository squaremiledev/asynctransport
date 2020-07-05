package com.michaelszymczak.sample;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FooMainTests {

    @Test
    void shouldFoo() {
        assertEquals("bar", new FooMain().foo());
    }
}
