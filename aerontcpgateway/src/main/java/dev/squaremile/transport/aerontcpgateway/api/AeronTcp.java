package dev.squaremile.transport.aerontcpgateway.api;

import dev.squaremile.asynctcp.api.serialization.SerializedMessageListener;
import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;

public class AeronTcp
{
    public TransportApplicationOnDuty createUsingExistingMediaDriver(
            final String role,
            final String aeronDirectoryName,
            final SerializedMessageListener serializedMessageListener,
            final TransportApplicationOnDutyFactory applicationFactory
    )
    {
        return create(role, serializedMessageListener, applicationFactory, new AeronTcpGateway(10, 11, aeronDirectoryName).start());
    }

    public TransportApplicationOnDuty createWithEmbeddedMediaDriver(
            final String role,
            final SerializedMessageListener serializedMessageListener,
            final TransportApplicationOnDutyFactory applicationFactory
    )
    {
        return create(role, serializedMessageListener, applicationFactory, new AeronTcpGateway(10, 11).start());
    }

    private TransportApplicationOnDuty create(
            final String role,
            final SerializedMessageListener serializedMessageListener,
            final TransportApplicationOnDutyFactory applicationFactory,
            final AeronTcpGateway gateway
    )
    {
        final AeronTcpGatewayClient gatewayClient = new AeronTcpGatewayClient(gateway.aeronConnection()).start();
        final TransportApplicationOnDuty application = gatewayClient.create(role, applicationFactory, serializedMessageListener);
        return new TransportApplicationOnDuty()
        {
            @Override
            public void onStart()
            {
                application.onStart();
            }

            @Override
            public void onStop()
            {
                application.onStop();
            }

            @Override
            public void work()
            {
                gateway.work();
                application.work();
            }

            @Override
            public void close()
            {
                application.close();
                gatewayClient.close();
                gateway.close();
            }

            @Override
            public void onEvent(final Event event)
            {
                application.onEvent(event);
            }
        };
    }
}
