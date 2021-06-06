package dev.squaremile.transport.aerontcpgateway.api;

import dev.squaremile.asynctcp.api.serialization.SerializedMessageListener;
import dev.squaremile.asynctcp.api.transport.app.AutoCloseableOnDuty;
import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;

public class AeronTcp
{
    public TransportApplicationOnDuty create(
            final String role,
            final SerializedMessageListener serializedMessageListener,
            final TransportApplicationOnDutyFactory applicationFactory,
            final DriverConfiguration driverConfiguration
    )
    {
        return create(role, serializedMessageListener, applicationFactory, AutoCloseableOnDuty.NO_OP, driverConfiguration);
    }

    public TransportApplicationOnDuty create(
            final String role,
            final SerializedMessageListener serializedMessageListener,
            final TransportApplicationOnDutyFactory applicationFactory
    )
    {
        final TcpDriver tcpDriver = createEmbeddedTcpDriver(10, 11).start();
        return create(role, serializedMessageListener, applicationFactory, tcpDriver, tcpDriver.configuration());
    }


    private TransportApplicationOnDuty create(
            final String role,
            final SerializedMessageListener serializedMessageListener,
            final TransportApplicationOnDutyFactory applicationFactory,
            final AutoCloseableOnDuty media,
            final DriverConfiguration driverConfiguration
    )
    {
        final Tcp gatewayClient = new Tcp(driverConfiguration).start();
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
                media.work();
                application.work();
            }

            @Override
            public void close()
            {
                application.close();
                gatewayClient.close();
                media.close();
            }

            @Override
            public void onEvent(final Event event)
            {
                application.onEvent(event);
            }
        };
    }

    public TcpDriver createTcpDriver(final int toNetworkStreamId, final int fromNetworStreamId, final String aeronDirectoryName)
    {
        return new TcpDriver(toNetworkStreamId, fromNetworStreamId, aeronDirectoryName);
    }

    public TcpDriver createEmbeddedTcpDriver(final int toNetworkStreamId, final int fromNetworStreamId)
    {
        return createTcpDriver(toNetworkStreamId, fromNetworStreamId, null);
    }
}
