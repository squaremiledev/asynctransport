package dev.squaremile.transport.aeroncluster.fixtures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


import dev.squaremile.transport.aeroncluster.api.IngressEndpoints;

class ClusterEndpointsTest
{
    @Test
    void shouldGenerateURIs()
    {
        assertEquals(
                "0,hostnameA:9002,hostnameA:9003,hostnameA:9004,hostnameA:9005,hostnameA:9001|" +
                "1,hostnameB:9102,hostnameB:9103,hostnameB:9104,hostnameB:9105,hostnameB:9101|" +
                "2,hostnameC:9202,hostnameC:9203,hostnameC:9204,hostnameC:9205,hostnameC:9201|",
                new ClusterEndpoints(
                        new ClusterEndpoints.NodeEndpoints(
                                0,
                                new IngressEndpoints.Endpoint(0, "hostnameA", 9002),
                                "hostnameA:9003",
                                "hostnameA:9004",
                                "hostnameA:9005",
                                "hostnameA:9001",
                                "localhost:9006",
                                "localhost:8030"
                        ),
                        new ClusterEndpoints.NodeEndpoints(
                                1,
                                new IngressEndpoints.Endpoint(1, "hostnameB", 9102),
                                "hostnameB:9103",
                                "hostnameB:9104",
                                "hostnameB:9105",
                                "hostnameB:9101",
                                "localhost:9006",
                                "localhost:8030"
                        ),
                        new ClusterEndpoints.NodeEndpoints(
                                2,
                                new IngressEndpoints.Endpoint(2, "hostnameC", 9202),
                                "hostnameC:9203",
                                "hostnameC:9204",
                                "hostnameC:9205",
                                "hostnameC:9201",
                                "localhost:9006",
                                "localhost:8030"
                        )
                ).asUri()
        );
    }
}