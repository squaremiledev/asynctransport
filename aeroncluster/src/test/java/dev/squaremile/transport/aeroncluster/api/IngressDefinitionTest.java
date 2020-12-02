package dev.squaremile.transport.aeroncluster.api;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngressDefinitionTest
{
    @Test
    void shouldGenerateUri()
    {
        IngressDefinition ingress = new IngressDefinition(Arrays.asList(
                new IngressDefinition.Endpoint(0, "hostnameA", 8880),
                new IngressDefinition.Endpoint(5, "hostnameB", 8885),
                new IngressDefinition.Endpoint(3, "hostnameC", 8883)
        ));

        assertEquals(
                "0=hostnameA:8880,5=hostnameB:8885,3=hostnameC:8883",
                ingress.asUri()
        );
    }
}