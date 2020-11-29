package dev.squaremile.aeroncluster.api;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngressEndpointsTest
{
    @Test
    void shouldGenerateUri()
    {
        IngressEndpoints ingressEndpoints = new IngressEndpoints(Arrays.asList(
                new IngressEndpoints.Endpoint(0, "hostnameA", 8880),
                new IngressEndpoints.Endpoint(5, "hostnameB", 8885),
                new IngressEndpoints.Endpoint(3, "hostnameC", 8883)
        ));

        assertEquals(
                "0=hostnameA:8880,5=hostnameB:8885,3=hostnameC:8883",
                ingressEndpoints.asUri()
        );
    }
}