package dev.squaremile.asynctcp.transport.api.app;

public interface TransportApplicationOnDutyFactory extends TransportApplicationFactory
{
    @Override
    TransportApplicationOnDuty create(Transport transport);
}
