package dev.squaremile.asynctcp.api.transport.app;

public interface TransportApplicationOnDutyFactory extends TransportApplicationFactory
{
    @Override
    TransportApplicationOnDuty create(Transport transport);
}
