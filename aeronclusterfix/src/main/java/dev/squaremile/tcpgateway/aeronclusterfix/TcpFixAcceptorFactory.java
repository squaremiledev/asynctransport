package dev.squaremile.tcpgateway.aeronclusterfix;

import dev.squaremile.asynctcp.api.transport.app.EventListener;
import dev.squaremile.asynctcp.api.wiring.ListeningApplication;
import dev.squaremile.asynctcp.fix.FixHandler;
import dev.squaremile.tcpgateway.aeroncluster.clusterclient.TcpGatewayConnection;
import dev.squaremile.tcpgateway.aeroncluster.clusterservice.EventHandler;
import dev.squaremile.tcpgateway.aeroncluster.clusterservice.TcpGatewayClient;
import dev.squaremile.transport.aeroncluster.api.IngressDefinition;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.fixMessage;
import static dev.squaremile.asynctcp.fix.FixHandlerFactory.createFixHandler;

class TcpFixAcceptorFactory
{
    public static EventHandler createClusteredTcpFixAcceptor(final int egressStreamId, final int tcpPort, final EventListener events, final FixHandler fixHandler)
    {
        return new TcpGatewayClient(egressStreamId, transport -> new ListeningApplication(transport, fixMessage(), tcpPort, events, createFixHandler(fixHandler)));
    }

    static TcpGatewayConnection createTcpGateway(final int ingressStreamId, final int egressStreamId, final IngressDefinition ingress, final String aeronDirectory)
    {
        return new TcpGatewayConnection(ingress, ingressStreamId, egressStreamId, aeronDirectory);
    }
}
