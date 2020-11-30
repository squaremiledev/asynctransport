package dev.squaremile.transport.aeroncluster.support.cluster;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        private final String archiveControl;
        private final int nodeId;
        private final String logControl;
        private final String recordingEvents;

        public NodeEndpoints(
                final int nodeId,
                final String ingress,
                final String consensus,
                final String log,
                final String catchup,
                final String archiveControl,
                final String logControl,
                final String recordingEvents
        )
        {
            this.archiveControl = archiveControl;
            this.nodeId = nodeId;
            this.ingress = ingress;
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
