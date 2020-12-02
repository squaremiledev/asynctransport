package dev.squaremile.transport.aeroncluster.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import static java.util.Collections.unmodifiableList;

public class IngressDefinition
{
    private final List<Endpoint> endpoints;

    public IngressDefinition(final List<Endpoint> endpoints)
    {
        if (endpoints.isEmpty())
        {
            throw new IllegalArgumentException("At least one ingress endpoint required");
        }
        this.endpoints = unmodifiableList(new ArrayList<>(endpoints));
    }

    public String asUri()
    {
        return endpoints.stream()
                .map(endpoint -> String.format("%d=%s", endpoint.nodeId(), endpoint.endpoint()))
                .collect(Collectors.joining(","));
    }

    @Override
    public String toString()
    {
        return "IngressEndpoints{" +
               "endpoints=" + endpoints +
               '}';
    }

    public static class Endpoint
    {
        private final int nodeId;
        private final String hostname;
        private final int port;

        public Endpoint(final int nodeId, final String hostname, final int port)
        {
            this.nodeId = nodeId;
            this.hostname = hostname;
            this.port = port;
        }

        public int nodeId()
        {
            return nodeId;
        }

        public String endpoint()
        {
            return hostname() + ":" + port();
        }

        public String hostname()
        {
            return hostname;
        }

        public int port()
        {
            return port;
        }
    }
}
