package dev.squaremile.transport.aeroncluster.fixtures;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


import dev.squaremile.transport.aeroncluster.api.IngressEndpoints;

public class ClusterEndpoints
{
    private final List<NodeEndpoints> nodes;

    public ClusterEndpoints(NodeEndpoints... nodes)
    {
        if (nodes.length == 0)
        {
            throw new IllegalArgumentException("At least one node required");
        }
        this.nodes = Arrays.asList(nodes);
    }

    public static NodeEndpoints nodeEndpoints(final int nodeId, final IngressEndpoints.Endpoint ingress, final List<String> endpoints)
    {
        return new NodeEndpoints(
                nodeId,
                ingress,
                endpoints.get(0),
                endpoints.get(1),
                endpoints.get(2),
                endpoints.get(3),
                endpoints.get(4),
                endpoints.get(5)
        );
    }

    public static List<String> withLocalhost(final List<Integer> nodeFreePorts)
    {
        return nodeFreePorts.stream().map(port -> "localhost:" + port).collect(Collectors.toList());
    }

    public IngressEndpoints ingressEndpoints()
    {
        return new IngressEndpoints(nodes.stream().map(node -> node.ingressEndpoint).collect(Collectors.toList()));
    }

    public NodeEndpoints node(final int nodeId)
    {
        return nodes.get(nodeId);
    }

    public String asUri()
    {
        return nodes.stream().map(Object::toString).collect(Collectors.joining("|")) + "|";
    }

    @Override
    public String toString()
    {
        return "ClusterEndpoints{" +
               "nodes=" + nodes +
               '}';
    }

    public static class NodeEndpoints
    {
        private final String ingress;
        private final String consensus;
        private final String log;
        private final String catchup;
        private final IngressEndpoints.Endpoint ingressEndpoint;
        private final String archiveControl;
        private final int nodeId;
        private final String logControl;
        private final String recordingEvents;

        public NodeEndpoints(
                final int nodeId,
                final IngressEndpoints.Endpoint ingress,
                final String consensus,
                final String log,
                final String catchup,
                final String archiveControl,
                final String logControl,
                final String recordingEvents
        )
        {
            ingressEndpoint = ingress;
            this.archiveControl = archiveControl;
            this.nodeId = nodeId;
            this.ingress = ingress.endpoint();
            this.consensus = consensus;
            this.log = log;
            this.catchup = catchup;
            this.logControl = logControl;
            this.recordingEvents = recordingEvents;
        }

        public String logControl()
        {
            return logControl;
        }

        public String ingress()
        {
            return ingress;
        }

        public String consensus()
        {
            return consensus;
        }

        public String log()
        {
            return log;
        }

        public String catchup()
        {
            return catchup;
        }

        public int nodeId()
        {
            return nodeId;
        }

        public String archiveControl()
        {
            return archiveControl;
        }

        @Override
        public String toString()
        {
            return String.format(
                    "%d,%s,%s,%s,%s,%s",
                    nodeId, ingress, consensus, log, catchup, archiveControl
            );
        }

        public String recordingEvents()
        {
            return recordingEvents;
        }
    }
}
