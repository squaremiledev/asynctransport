package dev.squaremile.asynctcp.transport.api.app;

public interface TransportOnDuty extends AutoCloseable, OnDuty
{
    @Override
    void close();
}
